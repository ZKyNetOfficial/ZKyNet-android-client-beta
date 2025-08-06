/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.viewmodel

import androidx.lifecycle.ViewModel
import com.zaneschepke.wireguardautotunnel.data.network.UserSupportApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VisionViewModel @Inject constructor(
    val userSupportApi: UserSupportApi
) : ViewModel()