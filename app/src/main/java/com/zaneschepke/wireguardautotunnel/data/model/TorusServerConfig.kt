/*
 * TORUS VPN - Custom VPN Client  
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.model

/**
 * Configuration data class for TORUS VPN servers.
 * This contains the backend API details and authentication tokens
 * needed to download WireGuard configuration files.
 */
data class TorusServerConfig(
    val id: String,
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
    val isEnabled: Boolean = true // Whether this server is available for connection
)

