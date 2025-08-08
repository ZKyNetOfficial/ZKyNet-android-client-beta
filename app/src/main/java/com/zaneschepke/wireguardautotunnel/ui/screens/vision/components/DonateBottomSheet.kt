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
import androidx.compose.ui.platform.LocalUriHandler
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
    val uriHandler = LocalUriHandler.current

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
            text = "This will take you to our secure GitHub support page where you can safely support the project."
        )
        
        BottomSheetPrimaryButton(
            text = "Proceed to GitHub Support",
            onClick = {
                uriHandler.openUri("https://github.com/ZKyNet")
                onDismiss()
            }
        )
    }
}