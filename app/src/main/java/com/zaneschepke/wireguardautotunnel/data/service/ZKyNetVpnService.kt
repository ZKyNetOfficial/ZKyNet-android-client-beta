/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for managing ZKyNet VPN server connections using UUID-based API.
 * Handles peer creation, verification, config downloads, and cleanup operations.
 */
@Singleton
class ZKyNetVpnService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val peerIdManager: PeerIdManager
) {
    
    companion object {
        private const val TAG = "ZKyNetVpnService"
        private const val CONFIG_FILE_EXTENSION = ".conf"
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Use internal app storage directory that doesn't require permissions
    private val configDirectory: File
        get() = File(context.filesDir, "zkynet_configs").apply {
            if (!exists()) mkdirs()
        }
    
    /**
     * API response models for peer verification
     */
    
    @Serializable
    data class VerifyPeerResponse(
        val assigned_ip: String? = null,
        val created_at: String? = null,
        val expires_at: String? = null,
        val status: String
    )
    
    /**
     * Downloads a fresh configuration with new UUID from server.
     * Uses the new unified /peers/config endpoint that handles peer creation and config generation in one call.
     * Used by ConfigValidationService when local config is invalid or missing.
     * 
     * @param serverConfig The server configuration to connect to
     * @return The file path of the configuration file, or null if failed
     */
    suspend fun downloadFreshConfig(serverConfig: ZKyNetServerConfig): String? = withContext(Dispatchers.IO) {
        try {
            Timber.i("Downloading fresh config for server: ${serverConfig.displayName}")
            
            // Single API call to create peer and get config - new streamlined endpoint
            val (configContent, generatedUuid) = downloadConfigFromUnifiedEndpoint(serverConfig)
            if (configContent == null || generatedUuid == null) {
                Timber.e("Failed to download config from unified endpoint for server: ${serverConfig.displayName}")
                return@withContext null
            }
            
            Timber.i("Downloaded config with UUID: $generatedUuid for ${serverConfig.displayName}")
            
            // Save config to local storage with UUID as filename  
            val configPath = saveConfigToStorage(configContent, "${generatedUuid}.conf")
            if (configPath == null) {
                Timber.e("Failed to save config for UUID: $generatedUuid")
                return@withContext null
            }
            
            // Store peer information for future use
            val timestamp = System.currentTimeMillis()
            peerIdManager.storePeerUuid(serverConfig, generatedUuid)
            peerIdManager.storeConfigPath(serverConfig, configPath)
            
            Timber.i("Successfully downloaded and stored fresh config for ${serverConfig.displayName}")
            return@withContext configPath
            
        } catch (e: Exception) {
            Timber.e(e, "Error downloading fresh config for ${serverConfig.displayName}")
            return@withContext null
        }
    }
    
    /**
     * Verifies if a peer UUID is still valid on the server.
     * Used by ConfigValidationService to check stored UUIDs.
     * 
     * @param serverConfig The server configuration
     * @param uuid The peer UUID to verify
     * @return true if valid, false otherwise
     */
    suspend fun verifyPeerUuid(serverConfig: ZKyNetServerConfig, uuid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Verifying peer UUID: $uuid for server: ${serverConfig.displayName}")
            
            val request = Request.Builder()
                .url("${serverConfig.apiUrl}/peers/$uuid/verify")
                .addHeader("Authorization", "Bearer ${serverConfig.token}")
                .get()
                .build()
            
            val response: Response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                if (response.code == 404) {
                    Timber.w("📍 Peer UUID not found on server: $uuid (expected for deleted peers)")
                    return@withContext false
                } else {
                    val errorBody = response.body?.string()
                    Timber.w("🚨 Peer verification failed for $uuid")
                    Timber.w("HTTP ${response.code}: ${response.message}")
                    Timber.w("Response body: $errorBody")
                    return@withContext false
                }
            }
            
            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                Timber.e("Empty response from peer verification")
                return@withContext false
            }
            
            val verifyResponse = json.decodeFromString<VerifyPeerResponse>(responseBody)
            val isValid = verifyResponse.status == "active"
            
            Timber.i("Peer UUID verification result: $isValid for $uuid (status: ${verifyResponse.status})")
            return@withContext isValid
            
        } catch (e: Exception) {
            Timber.e(e, "Error verifying peer UUID: $uuid")
            return@withContext false
        }
    }
    
    /**
     * Downloads configuration using the new unified /peers/config endpoint.
     * This endpoint creates a peer and returns the config in one call with automatic UUID generation.
     * 
     * @param serverConfig The server configuration
     * @return Pair of (config content, generated UUID) or (null, null) if failed
     */
    private suspend fun downloadConfigFromUnifiedEndpoint(serverConfig: ZKyNetServerConfig): Pair<String?, String?> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Downloading config from unified endpoint for server: ${serverConfig.displayName}")
            
            val request = Request.Builder()
                .url("${serverConfig.apiUrl}/peers/config")
                .addHeader("Authorization", "Bearer ${serverConfig.token}")
                .post("".toRequestBody("application/json; charset=utf-8".toMediaType())) // Empty POST body - server generates UUID automatically
                .build()
            
            val response: Response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Timber.e("🚨 Unified config download failed for ${serverConfig.displayName}")
                Timber.e("HTTP ${response.code}: ${response.message}")
                Timber.e("Response body: $errorBody")
                return@withContext Pair(null, null)
            }
            
            val configContent = response.body?.string()
            if (configContent.isNullOrBlank()) {
                Timber.e("Received empty config content from unified endpoint")
                return@withContext Pair(null, null)
            }
            
            // Extract UUID from Content-Disposition header or response headers
            val contentDisposition = response.header("Content-Disposition")
            val generatedUuid = extractUuidFromResponse(contentDisposition, response.headers.names())
            
            if (generatedUuid == null) {
                Timber.w("Could not extract UUID from response, generating fallback UUID")
                val fallbackUuid = "zkynet-${System.currentTimeMillis()}"
                return@withContext Pair(configContent, fallbackUuid)
            }
            
            Timber.i("Successfully downloaded config (${configContent.length} characters) with UUID: $generatedUuid")
            return@withContext Pair(configContent, generatedUuid)
            
        } catch (e: Exception) {
            Timber.e(e, "Error downloading config from unified endpoint for server: ${serverConfig.displayName}")
            return@withContext Pair(null, null)
        }
    }
    
    /**
     * Extracts UUID from response headers, primarily from Content-Disposition header.
     * Falls back to other headers if needed.
     * 
     * @param contentDisposition The Content-Disposition header value
     * @param headerNames All header names from the response
     * @return The extracted UUID or null if not found
     */
    private fun extractUuidFromResponse(contentDisposition: String?, headerNames: Set<String>): String? {
        // Try to extract UUID from Content-Disposition header (e.g., "attachment; filename=uuid.conf")
        contentDisposition?.let { disposition ->
            val filenameRegex = """filename[*]?=["']?([^"';,\s]+)["']?""".toRegex()
            val match = filenameRegex.find(disposition)
            match?.groupValues?.get(1)?.let { filename ->
                // Remove .conf extension if present
                val uuid = filename.removeSuffix(".conf")
                if (uuid.isNotBlank() && uuid != filename) {
                    return uuid
                }
            }
        }
        
        // Fallback: look for UUID-like pattern in any header name or value
        // This is a backup strategy in case the API returns UUID in a different header
        headerNames.forEach { headerName ->
            if (headerName.contains("uuid", ignoreCase = true) || 
                headerName.contains("peer", ignoreCase = true)) {
                // Could potentially extract UUID from header name/value if needed
            }
        }
        
        return null
    }
    
    /**
     * Saves the WireGuard configuration content to local storage.
     * Handles filename conflicts by appending incremental suffixes.
     * 
     * @param configContent The WireGuard configuration content
     * @param baseFileName The base filename to use
     * @return The full path of the saved file, or null if failed
     */
    private fun saveConfigToStorage(configContent: String, baseFileName: String): String? {
        try {
            Timber.i("Saving config content to internal storage")
            
            // Find available filename to handle conflicts
            val finalFileName = findAvailableFileName(baseFileName)
            val configFile = File(configDirectory, finalFileName)
            
            // Write config content to file
            configFile.writeText(configContent)
            
            Timber.i("Config file saved successfully: ${configFile.absolutePath}")
            return configFile.absolutePath
            
        } catch (e: IOException) {
            Timber.e(e, "Failed to save config file")
            return null
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied when saving config file")
            return null
        }
    }
    
    /**
     * Finds an available filename, handling naming conflicts by appending suffixes.
     * Example: myvpn.conf -> myvpn(1).conf -> myvpn(2).conf
     * 
     * @param baseFileName The base filename to use
     * @return An available filename that doesn't conflict with existing files
     */
    private fun findAvailableFileName(baseFileName: String): String {
        Timber.i("Finding available filename for: $baseFileName")
        
        val nameWithoutExtension = baseFileName.substringBeforeLast(".")
        val extension = baseFileName.substringAfterLast(".", "")
        
        var counter = 0
        var candidateFileName = baseFileName
        
        while (File(configDirectory, candidateFileName).exists()) {
            counter++
            candidateFileName = if (extension.isNotEmpty()) {
                "${nameWithoutExtension}(${counter}).$extension"
            } else {
                "${nameWithoutExtension}(${counter})"
            }
        }
        
        if (counter > 0) {
            Timber.i("Filename conflict resolved. Using: $candidateFileName")
        }
        
        return candidateFileName
    }
    
    
    /**
     * Validates if a config file contains valid WireGuard configuration.
     */
    private fun isValidWireGuardConfig(filePath: String): Boolean {
        return try {
            val configContent = File(filePath).readText()
            // Basic validation - check for required sections
            configContent.contains("[Interface]") && configContent.contains("[Peer]")
        } catch (e: Exception) {
            Timber.e(e, "Error validating config file: $filePath")
            false
        }
    }
    
    /**
     * Deletes a peer from the server using UUID.
     * Uses DELETE /peers/{uuid} API endpoint.
     * 
     * @param serverConfig The server configuration containing API details
     * @param uuid The peer UUID to delete from the server
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deletePeerByUuid(
        serverConfig: ZKyNetServerConfig,
        uuid: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.i("Deleting peer $uuid from server: ${serverConfig.displayName}")
            
            val request = Request.Builder()
                .url("${serverConfig.apiUrl}/peers/$uuid")
                .addHeader("Authorization", "Bearer ${serverConfig.token}")
                .delete()
                .build()
            
            val response: Response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                if (response.code == 404) {
                    Timber.w("Peer UUID not found on server (already deleted): $uuid")
                    return@withContext true // Consider this success
                } else {
                    Timber.w("Peer deletion failed with code: ${response.code} - ${response.message}")
                    return@withContext false
                }
            }
            
            Timber.i("Successfully deleted peer $uuid from server")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.w(e, "Error deleting peer from server (non-critical): $uuid")
            return@withContext false
        }
    }
    
    /**
     * Cleans up all data for a server configuration.
     * Deletes local config files and removes peer from server.
     * 
     * @param serverConfig Server configuration to clean up
     * @return true if cleanup was successful
     */
    suspend fun cleanupServerConfig(serverConfig: ZKyNetServerConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.i("Starting cleanup for server: ${serverConfig.displayName}")
            
            // Get stored peer info
            val peerInfo = peerIdManager.getCompletePeerInfo(serverConfig)
            
            if (peerInfo != null) {
                val (uuid, _, configPath) = peerInfo
                
                // Delete local config file if it exists
                if (configPath != null && File(configPath).exists()) {
                    val deleted = File(configPath).delete()
                    Timber.i("Local config file deleted: $deleted for ${serverConfig.displayName}")
                }
                
                // Delete remote peer configuration
                val remoteDeleted = deletePeerByUuid(serverConfig, uuid)
                if (!remoteDeleted) {
                    Timber.w("Remote peer deletion failed, but continuing...")
                }
            }
            
            // Clear all stored peer data
            val dataCleared = peerIdManager.clearServerData(serverConfig)
            if (!dataCleared) {
                Timber.w("Failed to clear peer data for ${serverConfig.displayName}")
            }
            
            Timber.i("Cleanup completed for server: ${serverConfig.displayName}")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Error during config cleanup for ${serverConfig.displayName}")
            return@withContext false
        }
    }
}