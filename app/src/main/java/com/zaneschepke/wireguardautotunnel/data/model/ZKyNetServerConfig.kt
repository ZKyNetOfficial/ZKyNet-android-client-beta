/*
 * ZKyNet VPN - Custom VPN Client  
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.model

/**
 * Configuration data class for ZKyNet VPN servers.
 * This contains the backend API details and authentication tokens
 * needed to download WireGuard configuration files using UUID-based peer management.
 */
data class ZKyNetServerConfig(
    val id: String, // Generated from apiUrl + displayName hash for internal identification
    val displayName: String,
    val location: String,
    val country: String,
    val apiUrl: String,
    val token: String,
    val isTestServer: Boolean = false,
    val hardcodedConfig: String? = null, // For test server with embedded config
    val pingEndpoint: String? = null, // Server endpoint for connection validation
    val retryAttempts: Int = 3, // Number of retry attempts on failure
    val retryDelayMs: Long = 2000, // Delay between retry attempts
    val connectionTimeoutMs: Long = 30000, // Connection timeout
    val isEnabled: Boolean = true, // Whether this server is available for connection
    val peerUuid: String? = null, // Server-generated UUID for this device/server combination
    val lastConfigUpdate: Long? = null // Timestamp of last config download
) {
    /**
     * Generates a unique server key for identification based on API URL and display name.
     * Used for internal storage and UUID mapping.
     */
    fun getServerKey(): String {
        return "${apiUrl}#${displayName}".hashCode().toString()
    }
    
    /**
     * Creates a copy of this config with updated peer information.
     */
    fun withPeerInfo(uuid: String, timestamp: Long): ZKyNetServerConfig {
        return copy(peerUuid = uuid, lastConfigUpdate = timestamp)
    }
    
    /**
     * Checks if this server has valid peer information.
     */
    fun hasPeerInfo(): Boolean {
        return !peerUuid.isNullOrBlank()
    }
}
