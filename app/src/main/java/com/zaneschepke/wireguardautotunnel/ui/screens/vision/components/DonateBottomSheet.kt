/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.vision.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.GlobalBottomSheet
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.BottomSheetHeader
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.BottomSheetInfoCard
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.BottomSheetPrimaryButton

@Composable
fun DonateBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    GlobalBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss
    ) {
        BottomSheetHeader(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = "Donate",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = "Support ZKyNet Development"
        )
        
        BottomSheetInfoCard(
            text = "Donations are currently unavailable. Please check back later for donation options."
        )
        
        BottomSheetPrimaryButton(
            text = "OK",
            onClick = {
                onDismiss()
            }
        )
    }
}