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
import android.content.SharedPreferences
import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages server-generated UUID persistence for peer management.
 * Handles mapping between servers and their assigned UUIDs from the API.
 */
@Singleton
class PeerIdManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "PeerIdManager"
        private const val PREFS_NAME = "zkynet_peer_ids"
        private const val UUID_PREFIX = "peer_uuid_"
        private const val TIMESTAMP_PREFIX = "peer_timestamp_"
        private const val CONFIG_PATH_PREFIX = "config_path_"
    }
    
    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Stores the server-generated UUID for a specific server configuration.
     * Also stores the timestamp of when this UUID was assigned.
     */
    fun storePeerUuid(serverConfig: ZKyNetServerConfig, uuid: String): Boolean {
        return try {
            val serverKey = serverConfig.getServerKey()
            val timestamp = System.currentTimeMillis()
            
            val success = sharedPrefs.edit()
                .putString(UUID_PREFIX + serverKey, uuid)
                .putLong(TIMESTAMP_PREFIX + serverKey, timestamp)
                .commit()
            
            if (success) {
                Timber.i("Stored UUID for server ${serverConfig.displayName}: $uuid")
            } else {
                Timber.e("Failed to store UUID for server ${serverConfig.displayName}")
            }
            
            success
        } catch (e: Exception) {
            Timber.e(e, "Error storing UUID for server ${serverConfig.displayName}")
            false
        }
    }
    
    /**
     * Retrieves the stored UUID for a server configuration.
     */
    fun getPeerUuid(serverConfig: ZKyNetServerConfig): String? {
        return try {
            val serverKey = serverConfig.getServerKey()
            val uuid = sharedPrefs.getString(UUID_PREFIX + serverKey, null)
            
            if (uuid != null) {
                Timber.d("Retrieved UUID for server ${serverConfig.displayName}: $uuid")
            } else {
                Timber.d("No stored UUID found for server ${serverConfig.displayName}")
            }
            
            uuid
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving UUID for server ${serverConfig.displayName}")
            null
        }
    }
    
    /**
     * Gets the timestamp when the UUID was stored for a server.
     */
    fun getPeerTimestamp(serverConfig: ZKyNetServerConfig): Long? {
        return try {
            val serverKey = serverConfig.getServerKey()
            val timestamp = sharedPrefs.getLong(TIMESTAMP_PREFIX + serverKey, -1L)
            
            if (timestamp != -1L) timestamp else null
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving timestamp for server ${serverConfig.displayName}")
            null
        }
    }
    
    /**
     * Stores the local config file path associated with a server's UUID.
     */
    fun storeConfigPath(serverConfig: ZKyNetServerConfig, configPath: String): Boolean {
        return try {
            val serverKey = serverConfig.getServerKey()
            val success = sharedPrefs.edit()
                .putString(CONFIG_PATH_PREFIX + serverKey, configPath)
                .commit()
            
            if (success) {
                Timber.d("Stored config path for server ${serverConfig.displayName}: $configPath")
            }
            
            success
        } catch (e: Exception) {
            Timber.e(e, "Error storing config path for server ${serverConfig.displayName}")
            false
        }
    }
    
    /**
     * Retrieves the stored config file path for a server.
     */
    fun getConfigPath(serverConfig: ZKyNetServerConfig): String? {
        return try {
            val serverKey = serverConfig.getServerKey()
            sharedPrefs.getString(CONFIG_PATH_PREFIX + serverKey, null)
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving config path for server ${serverConfig.displayName}")
            null
        }
    }
    
    /**
     * Removes all stored information for a specific server.
     * Used during cleanup operations.
     */
    fun clearServerData(serverConfig: ZKyNetServerConfig): Boolean {
        return try {
            val serverKey = serverConfig.getServerKey()
            val success = sharedPrefs.edit()
                .remove(UUID_PREFIX + serverKey)
                .remove(TIMESTAMP_PREFIX + serverKey)
                .remove(CONFIG_PATH_PREFIX + serverKey)
                .commit()
            
            if (success) {
                Timber.i("Cleared all data for server ${serverConfig.displayName}")
            } else {
                Timber.e("Failed to clear data for server ${serverConfig.displayName}")
            }
            
            success
        } catch (e: Exception) {
            Timber.e(e, "Error clearing data for server ${serverConfig.displayName}")
            false
        }
    }
    
    /**
     * Checks if a server has stored peer information.
     */
    fun hasStoredPeerInfo(serverConfig: ZKyNetServerConfig): Boolean {
        val uuid = getPeerUuid(serverConfig)
        return !uuid.isNullOrBlank()
    }
    
    /**
     * Gets complete peer information for a server.
     * Returns a triple of (uuid, timestamp, configPath) or null if not found.
     */
    fun getCompletePeerInfo(serverConfig: ZKyNetServerConfig): Triple<String, Long, String?>? {
        return try {
            val uuid = getPeerUuid(serverConfig) ?: return null
            val timestamp = getPeerTimestamp(serverConfig) ?: return null
            val configPath = getConfigPath(serverConfig)
            
            Triple(uuid, timestamp, configPath)
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving complete peer info for server ${serverConfig.displayName}")
            null
        }
    }
    
    /**
     * Updates the server config with stored peer information if available.
     */
    fun enrichServerConfigWithPeerInfo(serverConfig: ZKyNetServerConfig): ZKyNetServerConfig {
        return try {
            val uuid = getPeerUuid(serverConfig)
            val timestamp = getPeerTimestamp(serverConfig)
            
            if (uuid != null && timestamp != null) {
                serverConfig.withPeerInfo(uuid, timestamp)
            } else {
                serverConfig
            }
        } catch (e: Exception) {
            Timber.e(e, "Error enriching server config for ${serverConfig.displayName}")
            serverConfig
        }
    }
    
    /**
     * Lists all stored server keys for debugging purposes.
     */
    fun getAllStoredServers(): List<String> {
        return try {
            sharedPrefs.all.keys
                .filter { it.startsWith(UUID_PREFIX) }
                .map { it.removePrefix(UUID_PREFIX) }
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving all stored servers")
            emptyList()
        }
    }
    
    /**
     * Clears all stored peer data (for debugging or reset functionality).
     */
    fun clearAllData(): Boolean {
        return try {
            val success = sharedPrefs.edit().clear().commit()
            if (success) {
                Timber.i("Cleared all peer data")
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "Error clearing all peer data")
            false
        }
    }
}