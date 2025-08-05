package com.zaneschepke.wireguardautotunnel.domain.model

/**
 * Data class representing a TORUS VPN server.
 * Contains predefined server configurations for the TORUS VPN service.
 */
data class TorusServer(
    val id: String,
    val name: String,
    val location: String,
    val country: String,
    val countryCode: String, // ISO country code for flag display
    val endpoint: String,
    val publicKey: String,
    val isOnline: Boolean = true,
    val latency: Int? = null // in milliseconds
)