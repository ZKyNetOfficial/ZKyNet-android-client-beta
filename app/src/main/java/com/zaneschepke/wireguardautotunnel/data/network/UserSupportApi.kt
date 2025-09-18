/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.network

import com.zaneschepke.wireguardautotunnel.data.entity.SupportResponse

interface UserSupportApi {
    suspend fun submitEmailForUpdates(email: String): Result<SupportResponse>
    suspend fun submitNodeOperatorEmail(email: String): Result<SupportResponse>
    suspend fun checkApiHealth(): Result<Boolean>
}