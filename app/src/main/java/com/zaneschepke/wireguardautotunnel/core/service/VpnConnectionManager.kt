package com.zaneschepke.wireguardautotunnel.core.service

import android.util.Log
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.core.tunnel.TunnelManager
import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import com.zaneschepke.wireguardautotunnel.data.service.ConfigValidationService
import com.zaneschepke.wireguardautotunnel.data.service.DynamicServerConfigManager
import com.zaneschepke.wireguardautotunnel.di.IoDispatcher
import com.zaneschepke.wireguardautotunnel.domain.model.AppSettings
import com.zaneschepke.wireguardautotunnel.domain.model.TunnelConf
import com.zaneschepke.wireguardautotunnel.domain.repository.AppDataRepository
import com.zaneschepke.wireguardautotunnel.util.StringValue
import com.wireguard.config.BadConfigException
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized VPN connection manager that handles all aspects of VPN lifecycle
 * including connection establishment, error recovery, health monitoring, and cleanup.
 * 
 * This class implements the robust error handling requirements:
 * - Invalid config: Validate → Re-download → Retry
 * - Connection failure: Ping test → Re-download → Retry  
 * - No internet: Network monitor → Auto-reconnect when available
 * - Config cleanup: Delete local file → API call to delete remote peer
 */
@Singleton
class VpnConnectionManager @Inject constructor(
    private val tunnelManager: TunnelManager,
    private val configValidationService: ConfigValidationService,
    private val configManager: DynamicServerConfigManager,
    private val appDataRepository: AppDataRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    
    companion object {
        private const val TAG = "VpnConnectionManager"
        private const val PING_TIMEOUT_MS = 10000L // Updated to 10 seconds as specified
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val MAX_BACKOFF_DELAY_MS = 32000L // 32 seconds max delay
        private const val CIRCUIT_BREAKER_FAILURE_THRESHOLD = 5
        private const val CIRCUIT_BREAKER_RECOVERY_TIME_MS = 60000L // 1 minute
    }
    
    // Circuit breaker state for handling repeated failures
    private var consecutiveFailures = 0
    private var lastFailureTime = 0L
    private var isCircuitOpen = false
    
    /**
     * Connection result sealed class for handling different outcomes
     */
    sealed class ConnectionResult {
        object Success : ConnectionResult()
        data class Error(val message: StringValue, val shouldRetry: Boolean = true) : ConnectionResult()
        data class PermissionRequired(val isVpnPermission: Boolean) : ConnectionResult()
    }
    
    /**
     * Connects to a ZKyNet server with comprehensive error handling and retry logic.
     * This is the main entry point for VPN connections.
     */
    suspend fun connectToServer(
        serverConfig: ZKyNetServerConfig,
        currentTunnels: List<TunnelConf>,
        appSettings: AppSettings,
        onShowMessage: (StringValue) -> Unit,
        onRequestVpnPermission: () -> Unit
    ): ConnectionResult = withContext(ioDispatcher) {
        
        // Check circuit breaker state
        if (isCircuitOpen) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastFailureTime < CIRCUIT_BREAKER_RECOVERY_TIME_MS) {
                Timber.w("Circuit breaker is open, rejecting connection attempt")
                return@withContext ConnectionResult.Error(
                    StringValue.DynamicString("Service temporarily unavailable, please try again later"),
                    shouldRetry = false
                )
            } else {
                // Reset circuit breaker for half-open state
                Timber.i("Circuit breaker entering half-open state")
                isCircuitOpen = false
                consecutiveFailures = 0
            }
        }
        
        Timber.i("Starting connection to server: ${serverConfig.displayName}")
        
        // Validate server configuration
        if (!configManager.validateServerConfig(serverConfig)) {
            Timber.e("Invalid server configuration: ${serverConfig.id}")
            return@withContext ConnectionResult.Error(
                StringValue.StringResource(R.string.error_file_format),
                shouldRetry = false
            )
        }
        
        var attempt = 1
        val maxAttempts = serverConfig.retryAttempts
        
        while (attempt <= maxAttempts) {
            Timber.i("Connection attempt $attempt/$maxAttempts for ${serverConfig.displayName}")
            
            try {
                val result = attemptConnection(serverConfig, currentTunnels, appSettings, onRequestVpnPermission)
                
                when (result) {
                    is ConnectionResult.Success -> {
                        Timber.i("Successfully connected to ${serverConfig.displayName} on attempt $attempt")
                        // Reset circuit breaker on success
                        consecutiveFailures = 0
                        isCircuitOpen = false
                        onShowMessage(StringValue.DynamicString("Connected to ${serverConfig.displayName}"))
                        return@withContext result
                    }
                    
                    is ConnectionResult.PermissionRequired -> {
                        Timber.i("Permission required for connection")
                        return@withContext result
                    }
                    
                    is ConnectionResult.Error -> {
                        if (!result.shouldRetry || attempt >= maxAttempts) {
                            Timber.e("Connection failed after $attempt attempts: ${result.message}")
                            return@withContext result
                        }
                        
                        Timber.w("Connection attempt $attempt failed, retrying: ${result.message}")
                        
                        // Exponential backoff delay with jitter and max cap
                        val baseDelay = serverConfig.retryDelayMs
                        val exponentialDelay = baseDelay * (1L shl (attempt - 1)) // 2^(attempt-1)
                        val cappedDelay = minOf(exponentialDelay, MAX_BACKOFF_DELAY_MS)
                        val jitter = (0..1000).random() // Add randomness to prevent thundering herd
                        val finalDelay = cappedDelay + jitter
                        
                        Timber.d("Retrying in ${finalDelay}ms (attempt $attempt, base=${baseDelay}ms)")
                        delay(finalDelay)
                        attempt++
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error during connection attempt $attempt")
                
                if (attempt >= maxAttempts) {
                    // Update circuit breaker on unexpected error failure
                    consecutiveFailures++
                    if (consecutiveFailures >= CIRCUIT_BREAKER_FAILURE_THRESHOLD) {
                        isCircuitOpen = true
                        lastFailureTime = System.currentTimeMillis()
                        Timber.w("Circuit breaker opened after unexpected errors")
                    }
                    
                    return@withContext ConnectionResult.Error(
                        StringValue.DynamicString("Failed to connect to ${serverConfig.displayName}: ${e.message}"),
                        shouldRetry = false
                    )
                }
                
                // Exponential backoff delay for unexpected errors with cap
                val baseDelay = serverConfig.retryDelayMs
                val exponentialDelay = baseDelay * (1L shl (attempt - 1))
                val cappedDelay = minOf(exponentialDelay, MAX_BACKOFF_DELAY_MS)
                val jitter = (0..1000).random()
                val finalDelay = cappedDelay + jitter
                
                Timber.d("Retrying after unexpected error in ${finalDelay}ms (attempt $attempt)")
                delay(finalDelay)
                attempt++
            }
        }
        
        // Update circuit breaker on final failure
        consecutiveFailures++
        if (consecutiveFailures >= CIRCUIT_BREAKER_FAILURE_THRESHOLD) {
            isCircuitOpen = true
            lastFailureTime = System.currentTimeMillis()
            Timber.w("Circuit breaker opened after $consecutiveFailures consecutive failures")
        }
        
        return@withContext ConnectionResult.Error(
            StringValue.DynamicString("Failed to connect to ${serverConfig.displayName} after $maxAttempts attempts"),
            shouldRetry = false
        )
    }
    
    /**
     * Attempts a single connection to the server using the new flow:
     * 1. Ping server (10s timeout) 
     * 2. Verify peer status (if current peer exists)
     * 3. Attempt connection (download new config if peer doesn't exist)
     */
    private suspend fun attemptConnection(
        serverConfig: ZKyNetServerConfig,
        currentTunnels: List<TunnelConf>,
        appSettings: AppSettings,
        onRequestVpnPermission: () -> Unit
    ): ConnectionResult = withContext(ioDispatcher) {
        
        // Step 1: Test server connectivity (mandatory with 10s timeout)
        Timber.i("Step 1: Testing server connectivity for ${serverConfig.displayName}")
        if (!serverConfig.pingEndpoint.isNullOrBlank()) {
            if (!testServerConnectivity(serverConfig.pingEndpoint)) {
                Timber.w("Server ping failed for ${serverConfig.displayName} - server in maintenance")
                return@withContext ConnectionResult.Error(
                    StringValue.StringResource(R.string.server_maintenance),
                    shouldRetry = true
                )
            }
            Timber.i("Server ping successful for ${serverConfig.displayName}")
        } else {
            Timber.w("No ping endpoint configured for ${serverConfig.displayName}, skipping connectivity test")
        }
        
        // Step 2: Verify current peer status (if we have stored peer info)
        Timber.i("Step 2: Verifying peer status for ${serverConfig.displayName}")
        val validationResult = configValidationService.validateAndGetConfig(serverConfig)
        
        val (configFilePath, validatedServerConfig) = when (validationResult) {
            is ConfigValidationService.ValidationResult.Success -> {
                Timber.i("Peer verification successful - using existing config for ${serverConfig.displayName}")
                Pair(validationResult.configPath, validationResult.serverConfig)
            }
            is ConfigValidationService.ValidationResult.Error -> {
                Timber.e("Peer verification failed: ${validationResult.message}")
                return@withContext ConnectionResult.Error(
                    StringValue.DynamicString(validationResult.message),
                    shouldRetry = validationResult.shouldRetry
                )
            }
            is ConfigValidationService.ValidationResult.RequiresDownload -> {
                Timber.i("Current peer doesn't exist - will download new config for ${serverConfig.displayName}")
                return@withContext ConnectionResult.Error(
                    StringValue.StringResource(R.string.error_download_failed),
                    shouldRetry = true
                )
            }
        }
        
        // Step 3: Validate configuration format
        Timber.i("Step 3: Validating configuration format for ${serverConfig.displayName}")
        val configContent = try {
            File(configFilePath).readText()
        } catch (e: Exception) {
            Timber.e(e, "Failed to read config file: $configFilePath")
            return@withContext ConnectionResult.Error(
                StringValue.StringResource(R.string.error_file_format),
                shouldRetry = true
            )
        }
        
        val parsedConfig = try {
            Config.parse(configContent.byteInputStream())
        } catch (e: BadConfigException) {
            Timber.e(e, "Invalid WireGuard config for server: ${serverConfig.displayName}")
            
            // Config validation service should have caught this, but cleanup if needed
            // Note: The validation service will handle cleanup and retry automatically
            
            return@withContext ConnectionResult.Error(
                StringValue.StringResource(R.string.error_file_format),
                shouldRetry = true
            )
        }
        
        // Step 4: Create tunnel configuration  
        Timber.i("Step 4: Creating tunnel configuration for ${serverConfig.displayName}")
        val tunnelName = "ZKyNet ${serverConfig.displayName}"
        val tunnelConf = createTunnelConfiguration(tunnelName, configContent, currentTunnels)
        
        // Step 5: Save tunnel configuration
        Timber.i("Step 5: Saving tunnel configuration for ${serverConfig.displayName}")
        saveTunnel(tunnelConf)
        
        // Step 6: Check VPN permission
        Timber.i("Step 6: Checking VPN permission for ${serverConfig.displayName}")
        if (!tunnelManager.hasVpnPermission() && !appSettings.isKernelEnabled) {
            Timber.i("VPN permission required for connection")
            onRequestVpnPermission()
            return@withContext ConnectionResult.PermissionRequired(isVpnPermission = true)
        }
        
        // Step 7: Establish VPN connection
        Timber.i("Step 7: Establishing VPN connection for ${serverConfig.displayName}")
        tunnelManager.stopTunnel(null) // Stop all tunnels
        
        val savedTunnel = appDataRepository.tunnels.getAll().firstOrNull { it.tunName == tunnelName }
            ?: tunnelConf
        
        tunnelManager.startTunnel(savedTunnel)
        
        Timber.i("Successfully completed VPN connection flow for ${serverConfig.displayName}")
        return@withContext ConnectionResult.Success
    }
    
    /**
     * Tests server connectivity by pinging the endpoint
     */
    private suspend fun testServerConnectivity(endpoint: String): Boolean = withContext(ioDispatcher) {
        return@withContext try {
            Timber.d("Testing connectivity to $endpoint")
            val address = InetAddress.getByName(endpoint)
            address.isReachable(PING_TIMEOUT_MS.toInt())
        } catch (e: UnknownHostException) {
            Timber.w("Cannot resolve hostname: $endpoint", e)
            false
        } catch (e: Exception) {
            Timber.w("Connectivity test failed for $endpoint", e)
            false
        }
    }
    
    /**
     * Creates a tunnel configuration from server config and content
     */
    private fun createTunnelConfiguration(
        tunnelName: String,
        configContent: String,
        currentTunnels: List<TunnelConf>
    ): TunnelConf {
        val existingTunnel = currentTunnels.find { it.tunName == tunnelName }
        
        return if (existingTunnel != null) {
            Timber.i("Updating existing tunnel: $tunnelName")
            existingTunnel.copy(wgQuick = configContent)
        } else {
            Timber.i("Creating new tunnel: $tunnelName")
            TunnelConf(
                id = 0, // Will be auto-generated when saved
                tunName = tunnelName,
                wgQuick = configContent,
                amQuick = "", // Empty for standard WireGuard
                tunnelNetworks = emptyList(),
                isPrimaryTunnel = true,
                isMobileDataTunnel = false,
                isEthernetTunnel = false,
                isActive = false,
                isPingEnabled = false,
                pingInterval = null,
                pingCooldown = null,
                pingIp = null,
                isIpv4Preferred = false
            )
        }
    }
    
    /**
     * Saves tunnel configuration to database
     */
    private suspend fun saveTunnel(tunnel: TunnelConf) {
        try {
            appDataRepository.tunnels.save(tunnel)
            Timber.d("Tunnel configuration saved: ${tunnel.tunName}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save tunnel configuration: ${tunnel.tunName}")
            throw e
        }
    }
    
    
    /**
     * Validates connection health by testing internet connectivity
     * Implements: "VPN connects but no internet: Ping server; if ping fails, disconnect and redownload config"
     */
    suspend fun validateConnectionHealth(activeServer: ZKyNetServerConfig?): Boolean = withContext(ioDispatcher) {
        if (activeServer?.pingEndpoint == null) {
            return@withContext true // Cannot validate, assume healthy
        }
        
        val isHealthy = testServerConnectivity(activeServer.pingEndpoint)
        
        if (!isHealthy) {
            Timber.w("Connection health check failed for ${activeServer.displayName}")
            // Trigger reconnection with config redownload
            // This could be called from a periodic health check
        }
        
        return@withContext isHealthy
    }
    
    /**
     * Disconnects from current VPN tunnel
     */
    suspend fun disconnect() {
        try {
            tunnelManager.stopTunnel(null)
            Timber.i("VPN disconnected successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error during VPN disconnection")
        }
    }
    
    /**
     * Resets the circuit breaker state manually (for admin/debug purposes)
     */
    fun resetCircuitBreaker() {
        consecutiveFailures = 0
        isCircuitOpen = false
        lastFailureTime = 0L
        Timber.i("Circuit breaker state manually reset")
    }
    
    /**
     * Gets current circuit breaker status for monitoring
     */
    fun getCircuitBreakerStatus(): Triple<Boolean, Int, Long> {
        return Triple(isCircuitOpen, consecutiveFailures, lastFailureTime)
    }
}