/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.service

import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the complete validation flow for VPN configurations.
 * Works with the new streamlined API that uses unified /peers/config endpoint.
 * Implements the required flow:
 * 1. Check if local .conf exists for server
 * 2. If exists, verify UUID is still valid on server
 * 3. If invalid/missing, download fresh config with new UUID using unified endpoint
 * 4. Return validated config path for connection
 */
@Singleton
class ConfigValidationService @Inject constructor(
    private val peerIdManager: PeerIdManager,
    private val zkynetVpnService: ZKyNetVpnService
) {
    
    companion object {
        private const val TAG = "ConfigValidationService"
    }
    
    /**
     * Validation result sealed class
     */
    sealed class ValidationResult {
        data class Success(val configPath: String, val serverConfig: ZKyNetServerConfig) : ValidationResult()
        data class Error(val message: String, val shouldRetry: Boolean = true) : ValidationResult()
        object RequiresDownload : ValidationResult()
    }
    
    /**
     * Validation step enumeration for logging
     */
    enum class ValidationStep {
        CHECK_LOCAL_CONFIG,
        VERIFY_SERVER_CONFIG,
        DOWNLOAD_NEW_CONFIG,
        VALIDATE_CONFIG_FORMAT
    }
    
    /**
     * Main validation flow entry point.
     * Implements the complete validation sequence as specified.
     */
    suspend fun validateAndGetConfig(serverConfig: ZKyNetServerConfig): ValidationResult = withContext(Dispatchers.IO) {
        try {
            Timber.i("🚀 Starting config validation for server: ${serverConfig.displayName}")
            
            // Step 1: Check local configuration
            logValidationStep(ValidationStep.CHECK_LOCAL_CONFIG, serverConfig)
            val localConfigResult = checkLocalConfig(serverConfig)
            
            when (localConfigResult) {
                is LocalConfigResult.Found -> {
                    // Step 2: Verify server still has this UUID
                    logValidationStep(ValidationStep.VERIFY_SERVER_CONFIG, serverConfig)
                    val verificationResult = verifyServerConfig(localConfigResult.uuid, serverConfig)
                    
                    when (verificationResult) {
                        is ServerVerificationResult.Valid -> {
                            Timber.i("✅ Config validation successful for ${serverConfig.displayName}")
                            ValidationResult.Success(localConfigResult.configPath, localConfigResult.serverConfig)
                        }
                        is ServerVerificationResult.Invalid -> {
                            Timber.w("❌ Server config invalid, downloading fresh: ${verificationResult.reason}")
                            downloadFreshConfig(serverConfig)
                        }
                        is ServerVerificationResult.Error -> {
                            Timber.e("🚨 Server verification error: ${verificationResult.message}")
                            ValidationResult.Error(verificationResult.message, shouldRetry = true)
                        }
                    }
                }
                is LocalConfigResult.Missing -> {
                    Timber.i("No local config found, downloading fresh for ${serverConfig.displayName}")
                    downloadFreshConfig(serverConfig)
                }
                is LocalConfigResult.Invalid -> {
                    Timber.w("Local config invalid: ${localConfigResult.reason}, downloading fresh")
                    downloadFreshConfig(serverConfig)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during config validation for ${serverConfig.displayName}")
            ValidationResult.Error("Configuration validation failed: ${e.localizedMessage}", shouldRetry = true)
        }
    }
    
    /**
     * Step 1: Check local configuration status
     */
    private suspend fun checkLocalConfig(serverConfig: ZKyNetServerConfig): LocalConfigResult = withContext(Dispatchers.IO) {
        try {
            // Get stored peer info
            val peerInfo = peerIdManager.getCompletePeerInfo(serverConfig)
            if (peerInfo == null) {
                return@withContext LocalConfigResult.Missing("No stored peer information")
            }
            
            val (uuid, timestamp, configPath) = peerInfo
            
            // Check if config file exists
            if (configPath == null || !File(configPath).exists()) {
                Timber.w("Config file missing for ${serverConfig.displayName}, path: $configPath")
                return@withContext LocalConfigResult.Missing("Config file not found")
            }
            
            // Basic config file validation
            if (!isValidConfigFile(configPath)) {
                Timber.w("Config file invalid for ${serverConfig.displayName}")
                return@withContext LocalConfigResult.Invalid("Config file format invalid")
            }
            
            // Return enriched server config with peer info
            val enrichedConfig = serverConfig.withPeerInfo(uuid, timestamp)
            LocalConfigResult.Found(uuid, configPath, enrichedConfig)
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking local config for ${serverConfig.displayName}")
            LocalConfigResult.Invalid("Error accessing local config: ${e.localizedMessage}")
        }
    }
    
    /**
     * Step 2: Verify server still recognizes our UUID
     */
    private suspend fun verifyServerConfig(uuid: String, serverConfig: ZKyNetServerConfig): ServerVerificationResult = withContext(Dispatchers.IO) {
        try {
            val isValid = zkynetVpnService.verifyPeerUuid(serverConfig, uuid)
            
            if (isValid) {
                ServerVerificationResult.Valid
            } else {
                ServerVerificationResult.Invalid("Server does not recognize UUID")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error verifying server config for UUID: $uuid")
            ServerVerificationResult.Error("Server verification failed: ${e.localizedMessage}")
        }
    }
    
    /**
     * Step 3: Download fresh configuration with new UUID using streamlined API
     */
    private suspend fun downloadFreshConfig(serverConfig: ZKyNetServerConfig): ValidationResult = withContext(Dispatchers.IO) {
        try {
            logValidationStep(ValidationStep.DOWNLOAD_NEW_CONFIG, serverConfig)
            
            // Clean up old data first
            peerIdManager.clearServerData(serverConfig)
            
            // Get new config from server using unified /peers/config endpoint
            // This single call creates peer and returns config with auto-generated UUID
            val configPath = zkynetVpnService.downloadFreshConfig(serverConfig)
            
            if (configPath != null) {
                logValidationStep(ValidationStep.VALIDATE_CONFIG_FORMAT, serverConfig)
                
                // Validate the downloaded config
                if (isValidConfigFile(configPath)) {
                    Timber.i("Fresh config downloaded and validated for ${serverConfig.displayName}")
                    
                    // Get the updated server config with new peer info
                    val updatedConfig = peerIdManager.enrichServerConfigWithPeerInfo(serverConfig)
                    ValidationResult.Success(configPath, updatedConfig)
                } else {
                    Timber.e("Downloaded config is invalid for ${serverConfig.displayName}")
                    ValidationResult.Error("Downloaded configuration is invalid", shouldRetry = true)
                }
            } else {
                Timber.e("Failed to download config for ${serverConfig.displayName}")
                ValidationResult.Error("Unable to download configuration", shouldRetry = true)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error downloading fresh config for ${serverConfig.displayName}")
            ValidationResult.Error("Config download failed: ${e.localizedMessage}", shouldRetry = true)
        }
    }
    
    /**
     * Basic validation of config file format
     */
    private fun isValidConfigFile(configPath: String): Boolean {
        return try {
            val configContent = File(configPath).readText()
            // Basic WireGuard config validation
            configContent.contains("[Interface]") && configContent.contains("[Peer]")
        } catch (e: Exception) {
            Timber.e(e, "Error validating config file: $configPath")
            false
        }
    }
    
    /**
     * Logs validation steps for troubleshooting
     */
    private fun logValidationStep(step: ValidationStep, serverConfig: ZKyNetServerConfig) {
        val message = when (step) {
            ValidationStep.CHECK_LOCAL_CONFIG -> 
                "Checking local config for server: ${serverConfig.displayName}"
            ValidationStep.VERIFY_SERVER_CONFIG -> 
                "Verifying server config for: ${serverConfig.displayName}"
            ValidationStep.DOWNLOAD_NEW_CONFIG -> 
                "Downloading new config for: ${serverConfig.displayName}"
            ValidationStep.VALIDATE_CONFIG_FORMAT -> 
                "Validating config format for: ${serverConfig.displayName}"
        }
        
        Timber.i("📋 ConfigValidation: $message")
    }
    
    /**
     * Helper sealed classes for internal flow control
     */
    private sealed class LocalConfigResult {
        data class Found(val uuid: String, val configPath: String, val serverConfig: ZKyNetServerConfig) : LocalConfigResult()
        data class Missing(val reason: String) : LocalConfigResult()
        data class Invalid(val reason: String) : LocalConfigResult()
    }
    
    private sealed class ServerVerificationResult {
        object Valid : ServerVerificationResult()
        data class Invalid(val reason: String) : ServerVerificationResult()
        data class Error(val message: String) : ServerVerificationResult()
    }
}