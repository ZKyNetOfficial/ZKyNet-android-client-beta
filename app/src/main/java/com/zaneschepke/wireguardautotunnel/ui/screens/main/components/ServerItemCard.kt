/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.ui.navigation.LocalIsAndroidTV

/**
 * Server types for styling and behavior differentiation
 */
enum class ServerType {
    ZKYNET_REGULAR,
    ZKYNET_TEST,
    MANUAL
}

/**
 * Connection status for proper error handling and user feedback
 */
enum class ConnectionStatus {
    CONNECTED,
    AVAILABLE,
    CONNECTING,
    FAILED,
    CONFIG_ERROR,
    NETWORK_ERROR
}

/**
 * Data class representing a server for UI display
 */
data class ServerDisplayInfo(
    val id: String,
    val displayName: String,
    val location: String,
    val country: String = "",
    val serverType: ServerType,
    val connectionStatus: ConnectionStatus,
    val errorMessage: String? = null
)

/**
 * Reusable server item card component that handles both TORUS servers and manual tunnels.
 * Provides proper error handling, status management, and consistent styling.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServerItemCard(
    server: ServerDisplayInfo,
    onConnect: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isConnected = server.connectionStatus == ConnectionStatus.CONNECTED
    val isError = server.connectionStatus in listOf(
        ConnectionStatus.FAILED,
        ConnectionStatus.CONFIG_ERROR,
        ConnectionStatus.NETWORK_ERROR
    )
    val isConnecting = server.connectionStatus == ConnectionStatus.CONNECTING
    val isTv = LocalIsAndroidTV.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Context menu state for manual tunnels
    var showContextMenu by remember { mutableStateOf(false) }
    val showMenuForManualTunnel = server.serverType == ServerType.MANUAL && onDelete != null
    
    val clickAction: () -> Unit = when {
        isConnected -> fun() { } // No action when connected
        isError && onRetry != null -> onRetry
        !isError -> onConnect
        else -> fun() { }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isTv) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = clickAction,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (showMenuForManualTunnel) {
                                showContextMenu = true
                            } else if (onLongPress != null) {
                                onLongPress()
                            }
                        }
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = getContainerColor(server.serverType, server.connectionStatus)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isConnected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection status indicator or loading
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = getIconColor(server.serverType, server.connectionStatus)
                    )
                } else {
                    Icon(
                        imageVector = getServerIcon(server.serverType),
                        contentDescription = "Server Icon",
                        tint = getIconColor(server.serverType, server.connectionStatus)
                    )
                }
                
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = server.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = getTextColor(server.serverType, server.connectionStatus)
                        )
                        
                        // Server type badge
                        when (server.serverType) {
                            ServerType.ZKYNET_TEST -> {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ) {
                                    Text(
                                        text = "TEST",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                            ServerType.MANUAL -> {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ) {
                                    Text(
                                        text = "MANUAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                            ServerType.ZKYNET_REGULAR -> {
                                // No badge for regular ZKyNet servers
                            }
                        }
                    }
                    
                    // Location and error message
                    val locationText = if (server.country.isNotEmpty()) {
                        "${server.location}, ${server.country}"
                    } else {
                        server.location
                    }
                    
                    Text(
                        text = if (isError && server.errorMessage != null) {
                            server.errorMessage
                        } else {
                            locationText
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            getLocationTextColor(server.serverType, server.connectionStatus)
                        }
                    )
                    
                    // Additional action buttons for errors
                    if (isError) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (onRetry != null) {
                                TextButton(
                                    onClick = onRetry,
                                    colors = ButtonDefaults.textButtonColors()
                                ) {
                                    Text(
                                        text = "Retry",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            
                            if (onEdit != null && server.serverType == ServerType.MANUAL) {
                                TextButton(
                                    onClick = onEdit,
                                    colors = ButtonDefaults.textButtonColors()
                                ) {
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Status text
            Text(
                text = getStatusText(server.connectionStatus),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = getStatusTextColor(server.serverType, server.connectionStatus)
            )
        }
        
        // Context menu for manual tunnels
        if (showMenuForManualTunnel) {
            ServerContextMenu(
                expanded = showContextMenu,
                onDismiss = { showContextMenu = false },
                onDelete = { onDelete?.invoke() },
                onSettings = { onEdit?.invoke() ?: onLongPress?.invoke() }
            )
        }
    }
}

@Composable
private fun getContainerColor(serverType: ServerType, status: ConnectionStatus): androidx.compose.ui.graphics.Color {
    return when {
        status == ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
        status in listOf(ConnectionStatus.FAILED, ConnectionStatus.CONFIG_ERROR, ConnectionStatus.NETWORK_ERROR) -> 
            MaterialTheme.colorScheme.errorContainer
        serverType == ServerType.ZKYNET_TEST -> MaterialTheme.colorScheme.secondaryContainer
        serverType == ServerType.MANUAL -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
}

@Composable
private fun getIconColor(serverType: ServerType, status: ConnectionStatus): androidx.compose.ui.graphics.Color {
    return when {
        status == ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
        status in listOf(ConnectionStatus.FAILED, ConnectionStatus.CONFIG_ERROR, ConnectionStatus.NETWORK_ERROR) -> 
            MaterialTheme.colorScheme.error
        serverType == ServerType.ZKYNET_TEST -> MaterialTheme.colorScheme.secondary
        serverType == ServerType.MANUAL -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun getTextColor(serverType: ServerType, status: ConnectionStatus): androidx.compose.ui.graphics.Color {
    return when {
        status == ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.onPrimaryContainer
        status in listOf(ConnectionStatus.FAILED, ConnectionStatus.CONFIG_ERROR, ConnectionStatus.NETWORK_ERROR) -> 
            MaterialTheme.colorScheme.onErrorContainer
        serverType == ServerType.ZKYNET_TEST -> MaterialTheme.colorScheme.onSecondaryContainer
        serverType == ServerType.MANUAL -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun getLocationTextColor(serverType: ServerType, status: ConnectionStatus): androidx.compose.ui.graphics.Color {
    return when {
        status == ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        status in listOf(ConnectionStatus.FAILED, ConnectionStatus.CONFIG_ERROR, ConnectionStatus.NETWORK_ERROR) -> 
            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
        serverType == ServerType.ZKYNET_TEST -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        serverType == ServerType.MANUAL -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun getStatusTextColor(serverType: ServerType, status: ConnectionStatus): androidx.compose.ui.graphics.Color {
    return when {
        status == ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
        status in listOf(ConnectionStatus.FAILED, ConnectionStatus.CONFIG_ERROR, ConnectionStatus.NETWORK_ERROR) -> 
            MaterialTheme.colorScheme.error
        serverType == ServerType.ZKYNET_TEST -> MaterialTheme.colorScheme.secondary
        serverType == ServerType.MANUAL -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun getServerIcon(serverType: ServerType): ImageVector {
    return when (serverType) {
        ServerType.ZKYNET_TEST -> Icons.Outlined.Science
        ServerType.MANUAL -> Icons.Outlined.Settings
        ServerType.ZKYNET_REGULAR -> Icons.Outlined.LocationOn
    }
}

private fun getStatusText(status: ConnectionStatus): String {
    return when (status) {
        ConnectionStatus.CONNECTED -> "Connected"
        ConnectionStatus.AVAILABLE -> "Available"
        ConnectionStatus.CONNECTING -> "Connecting..."
        ConnectionStatus.FAILED -> "Failed"
        ConnectionStatus.CONFIG_ERROR -> "Config Error"
        ConnectionStatus.NETWORK_ERROR -> "Network Error"
    }
}