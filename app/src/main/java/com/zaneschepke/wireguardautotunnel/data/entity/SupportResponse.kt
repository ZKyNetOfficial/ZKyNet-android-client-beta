/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class SupportResponse(
    val message: String,
    val email: String? = null,
    val duplicate: Boolean = false
)

@Serializable
data class SupportRequest(
    val email: String
)

@Serializable
data class ApiHealth(
    val status: String,
    val message: String,
    val version: String
)