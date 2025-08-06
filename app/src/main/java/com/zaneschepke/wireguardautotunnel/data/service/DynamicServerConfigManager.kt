package com.zaneschepke.wireguardautotunnel.data.service

import android.content.Context
import android.util.Log
import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.Charsets

/**
 * Configuration wrapper for JSON deserialization
 */
@Serializable
private data class ServerConfigResponse(
    val version: String,
    val defaultRetryPolicy: DefaultRetryPolicy,
    val servers: List<ServerConfigJson>
)

@Serializable
private data class DefaultRetryPolicy(
    val maxAttempts: Int,
    val backoffMs: Long,
    val connectionTimeoutMs: Long
)

@Serializable
private data class ServerConfigJson(
    val id: String,
    val displayName: String,
    val location: String,
    val country: String,
    val apiUrl: String,
    val token: String,
    val isTestServer: Boolean = false,
    val pingEndpoint: String? = null,
    val retryAttempts: Int = 3,
    val retryDelayMs: Long = 2000,
    val connectionTimeoutMs: Long = 30000,
    val isEnabled: Boolean = true
) {
    fun toZKyNetServerConfig(): ZKyNetServerConfig {
        return ZKyNetServerConfig(
            id = id,
            displayName = displayName,
            location = location,
            country = country,
            apiUrl = apiUrl,
            token = token,
            isTestServer = isTestServer,
            hardcodedConfig = null, // Not used in dynamic configs
            pingEndpoint = pingEndpoint,
            retryAttempts = retryAttempts,
            retryDelayMs = retryDelayMs,
            connectionTimeoutMs = connectionTimeoutMs,
            isEnabled = isEnabled
        )
    }
}

/**
 * Manages dynamic server configurations loaded from JSON files.
 * Replaces the hardcoded TorusServersConfig with a flexible,
 * file-based configuration system that supports hot-reloading.
 */
@Singleton
class DynamicServerConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "DynamicServerConfigManager"
        private const val CONFIG_FILE_NAME = "server_configs.json"
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes cache
    }
    
    // Configuration caching
    private var lastLoadTime = 0L
    private var cachedConfigJson: String? = null
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Backing properties for proper StateFlow patterns
    private val _servers = MutableStateFlow<List<ZKyNetServerConfig>>(emptyList())
    val servers: StateFlow<List<ZKyNetServerConfig>> = _servers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    // Combined loading state for UI
    val configurationState: StateFlow<ConfigurationState> = combine(
        _servers,
        _isLoading,
        _lastError
    ) { servers, loading, error ->
        when {
            loading -> ConfigurationState.Loading
            error != null -> ConfigurationState.Error(error)
            servers.isEmpty() -> ConfigurationState.Empty
            else -> ConfigurationState.Success(servers)
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConfigurationState.Loading
    )
    
    /**
     * Sealed class representing different configuration states
     */
    sealed class ConfigurationState {
        object Loading : ConfigurationState()
        object Empty : ConfigurationState()
        data class Success(val servers: List<ZKyNetServerConfig>) : ConfigurationState()
        data class Error(val message: String) : ConfigurationState()
    }
    
    /**
     * Loads server configurations from the JSON file in assets.
     * This method is called during app initialization and can be called
     * again to reload configurations dynamically.
     */
    suspend fun loadConfigurations(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            _isLoading.value = true
            _lastError.value = null
            
            Log.i(TAG, "Loading server configurations from $CONFIG_FILE_NAME")
            
            // Check cache first (for reload scenarios)
            val currentTime = System.currentTimeMillis()
            val configJson = if (cachedConfigJson != null && 
                                (currentTime - lastLoadTime) < CACHE_DURATION_MS) {
                Log.d(TAG, "Using cached configuration")
                cachedConfigJson!!
            } else {
                // Read JSON file from assets with proper resource management and modern Android API support
                val freshJson = try {
                    context.assets.open(CONFIG_FILE_NAME).use { inputStream ->
                        inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                            reader.readText()
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception accessing asset file (API 29+)", e)
                    throw IOException("Asset access denied", e)
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "Configuration file not found in assets", e)
                    throw IOException("Configuration file missing", e)
                }
                
                // Update cache
                cachedConfigJson = freshJson
                lastLoadTime = currentTime
                freshJson
            }
            
            // Parse JSON configuration with better error handling
            val configResponse = json.decodeFromString<ServerConfigResponse>(configJson)
            
            // Validate configuration version
            if (configResponse.version.isEmpty()) {
                Log.w(TAG, "Configuration file missing version information")
            }
            
            // Convert to ZKyNetServerConfig objects with validation
            val serverConfigs = configResponse.servers
                .filter { it.isEnabled } // Only include enabled servers
                .mapNotNull { serverJson ->
                    try {
                        val config = serverJson.toZKyNetServerConfig()
                        if (validateServerConfig(config)) {
                            config
                        } else {
                            Log.w(TAG, "Invalid server configuration: ${serverJson.id}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting server config: ${serverJson.id}", e)
                        null
                    }
                }
            
            if (serverConfigs.isEmpty()) {
                _lastError.value = "No valid server configurations found"
                false
            } else {
                Log.i(TAG, "Successfully loaded ${serverConfigs.size} server configurations")
                _servers.value = serverConfigs
                
                // Log loaded servers for debugging
                serverConfigs.forEach { server ->
                    Log.d(TAG, "Loaded server: ${server.displayName} (${server.id}) - Enabled: ${server.isEnabled}")
                }
                true
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read config file: $CONFIG_FILE_NAME", e)
            _lastError.value = "Configuration file not found or inaccessible"
            false
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e(TAG, "Failed to parse config JSON", e)
            _lastError.value = "Invalid configuration file format"
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading configurations", e)
            _lastError.value = "Failed to load configurations: ${e.localizedMessage}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Gets a server configuration by ID.
     */
    fun getServerById(id: String): ZKyNetServerConfig? {
        return _servers.value.find { it.id == id }
    }
    
    /**
     * Gets the test server configuration.
     */
    fun getTestServer(): ZKyNetServerConfig? {
        return _servers.value.find { it.isTestServer }
    }
    
    /**
     * Gets all available servers (enabled only).
     */
    fun getAvailableServers(): List<ZKyNetServerConfig> {
        return _servers.value.filter { it.isEnabled }
    }
    
    /**
     * Reloads configurations from the JSON file.
     * Useful for updating configurations without restarting the app.
     */
    suspend fun reloadConfigurations(): Boolean {
        Log.i(TAG, "Reloading server configurations")
        return loadConfigurations()
    }
    
    /**
     * Validates a server configuration.
     */
    fun validateServerConfig(server: ZKyNetServerConfig): Boolean {
        return server.id.isNotBlank() &&
                server.displayName.isNotBlank() &&
                server.apiUrl.isNotBlank() &&
                server.token.isNotBlank() &&
                server.retryAttempts > 0 &&
                server.retryDelayMs > 0 &&
                server.connectionTimeoutMs > 0
    }
    
    /**
     * Gets configuration loading status.
     */
    fun isConfigurationLoaded(): Boolean {
        return _servers.value.isNotEmpty()
    }
}