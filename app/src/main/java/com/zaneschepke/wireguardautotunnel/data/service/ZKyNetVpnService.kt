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
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for managing ZKyNet VPN server connections.
 * Handles config file management, API downloading, and local storage operations.
 */
@Singleton
class ZKyNetVpnService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "ZKyNetVpnService"
        private const val CONFIG_FILE_EXTENSION = ".conf"
    }
    
    // Use internal app storage directory that doesn't require permissions
    private val configDirectory: File
        get() = File(context.filesDir, "zkynet_configs").apply {
            if (!exists()) mkdirs()
        }
    
    /**
     * Retrieves a WireGuard configuration for the specified server.
     * First checks for existing local file, then downloads from API if needed.
     * 
     * @param serverConfig The server configuration to connect to
     * @return The file path of the configuration file, or null if failed
     */
    suspend fun getServerConfig(serverConfig: ZKyNetServerConfig): String? = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting config retrieval for server: ${serverConfig.id}")
        
        // All servers now download configs from their respective API endpoints
        Log.i(TAG, "Processing server: ${serverConfig.displayName} via API: ${serverConfig.apiUrl}")
        
        // Check for existing config file first
        val configFileName = "${serverConfig.displayName.lowercase().replace(" ", "")}.conf"
        val existingConfigPath = checkForExistingConfig(configFileName)
        
        if (existingConfigPath != null) {
            Log.i(TAG, "Found existing config file: $existingConfigPath")
            return@withContext existingConfigPath
        }
        
        // Download new config from API
        Log.i(TAG, "No existing config found, downloading from API")
        return@withContext downloadConfigFromApi(serverConfig, configFileName)
    }
    
    
    /**
     * Checks if a WireGuard configuration file already exists locally.
     * 
     * @param fileName The base filename to check for
     * @return The full path of the existing file, or null if not found
     */
    private fun checkForExistingConfig(fileName: String): String? {
        Log.i(TAG, "Checking for existing config file: $fileName")
        
        val configFile = File(configDirectory, fileName)
        
        return if (configFile.exists() && configFile.isFile) {
            Log.i(TAG, "Existing config file found: ${configFile.absolutePath}")
            configFile.absolutePath
        } else {
            Log.i(TAG, "No existing config file found for: $fileName")
            null
        }
    }
    
    /**
     * Downloads WireGuard configuration from the server API using single auto-creation endpoint.
     * Uses GET /api/peer/{peer_id}/config which automatically creates the peer if it doesn't exist.
     * This is the recommended approach from the backend API documentation.
     * 
     * @param serverConfig The server configuration containing API details
     * @param baseFileName The base filename for saving the config
     * @return The full path of the saved config file, or null if failed
     */
    private suspend fun downloadConfigFromApi(
        serverConfig: ZKyNetServerConfig,
        baseFileName: String
    ): String? = withContext(Dispatchers.IO) {
        
        try {
            Log.i(TAG, "Starting single-call config download with auto-creation for: ${serverConfig.displayName}")
            
            // Generate consistent peer ID for this device/server combination
            val peerId = "android-${serverConfig.id}-user"
            
            Log.i(TAG, "Using peer ID: $peerId for server: ${serverConfig.apiUrl}")
            
            // Single API call that auto-creates peer and returns config
            val configContent = downloadConfigWithAutoCreation(serverConfig, peerId)
            if (configContent == null) {
                Log.e(TAG, "Failed to download/create config for peer: $peerId")
                return@withContext null
            }
            
            Log.i(TAG, "Successfully downloaded config for peer: $peerId")
            
            // Save config to local storage
            return@withContext saveConfigToStorage(configContent, baseFileName)
            
        } catch (e: IOException) {
            Log.e(TAG, "Network error while downloading config", e)
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while downloading config", e)
            return@withContext null
        }
    }
    
    /**
     * Downloads configuration using the auto-creation endpoint.
     * Single API call that creates the peer if it doesn't exist and returns the config.
     * This matches the recommended backend integration pattern.
     * 
     * @param serverConfig The server configuration
     * @param peerId The peer ID to use (creates if doesn't exist)
     * @return The config content if successful, null otherwise
     */
    private suspend fun downloadConfigWithAutoCreation(
        serverConfig: ZKyNetServerConfig, 
        peerId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Downloading config with auto-creation for peer: $peerId")
            
            // Build API request for auto-creation config download
            val request = Request.Builder()
                .url("${serverConfig.apiUrl}/api/peer/$peerId/config")
                .addHeader("Authorization", "Bearer ${serverConfig.token}")
                .get()
                .build()
            
            // Execute HTTP request
            val response: Response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Config auto-creation failed with code: ${response.code} - ${response.message}")
                val errorBody = response.body?.string()
                Log.e(TAG, "Error response: $errorBody")
                return@withContext null
            }
            
            val configContent = response.body?.string()
            if (configContent.isNullOrBlank()) {
                Log.e(TAG, "Received empty config content")
                return@withContext null
            }
            
            Log.i(TAG, "Successfully downloaded config with auto-creation (${configContent.length} characters)")
            return@withContext configContent
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading config with auto-creation", e)
            return@withContext null
        }
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
            Log.i(TAG, "Saving config content to internal storage")
            
            // Find available filename to handle conflicts
            val finalFileName = findAvailableFileName(baseFileName)
            val configFile = File(configDirectory, finalFileName)
            
            // Write config content to file
            configFile.writeText(configContent)
            
            Log.i(TAG, "Config file saved successfully: ${configFile.absolutePath}")
            return configFile.absolutePath
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save config file", e)
            return null
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied when saving config file", e)
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
        Log.i(TAG, "Finding available filename for: $baseFileName")
        
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
            Log.i(TAG, "Filename conflict resolved. Using: $candidateFileName")
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
            Log.e(TAG, "Error validating config file: $filePath", e)
            false
        }
    }
    
    /**
     * Deletes a peer configuration from the server remotely.
     * This implements the cleanup requirement: "Send an API call to the server to delete the old config remotely"
     * 
     * @param serverConfig The server configuration containing API details
     * @param peerId The peer ID to delete from the server
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deletePeerFromServer(
        serverConfig: ZKyNetServerConfig,
        peerId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Deleting peer $peerId from server: ${serverConfig.displayName}")
            
            // Build DELETE API request for peer removal
            val request = Request.Builder()
                .url("${serverConfig.apiUrl}/api/peer/$peerId")
                .addHeader("Authorization", "Bearer ${serverConfig.token}")
                .delete()
                .build()
            
            // Execute HTTP request
            val response: Response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.w(TAG, "Peer deletion failed with code: ${response.code} - ${response.message}")
                // Log but don't fail - this is cleanup, not critical
                return@withContext false
            }
            
            Log.i(TAG, "Successfully deleted peer $peerId from server")
            return@withContext true
            
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting peer from server (non-critical)", e)
            return@withContext false
        }
    }
    
    /**
     * Deletes old configuration files and cleans up remote peer.
     * Implements the full cleanup requirement from instructions.txt
     * 
     * @param oldConfigPath Path to the local config file to delete
     * @param serverConfig Server configuration for remote cleanup
     */
    suspend fun cleanupOldConfig(
        oldConfigPath: String,
        serverConfig: ZKyNetServerConfig
    ): Boolean = withContext(Dispatchers.IO) {
        var success = true
        
        try {
            // Delete local config file
            val configFile = File(oldConfigPath)
            if (configFile.exists()) {
                val deleted = configFile.delete()
                Log.i(TAG, "Local config file deleted: $deleted for ${serverConfig.displayName}")
                if (!deleted) success = false
            }
            
            // Generate the same peer ID that was used for creation
            val peerId = "android-${serverConfig.id}-user"
            
            // Delete remote peer configuration
            val remoteDeleted = deletePeerFromServer(serverConfig, peerId)
            if (!remoteDeleted) {
                Log.w(TAG, "Remote peer deletion failed, but continuing...")
                // Don't fail the entire operation for remote cleanup issues
            }
            
            return@withContext success
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during config cleanup", e)
            return@withContext false
        }
    }
}