/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.domain.model.TunnelConf
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.GlobalBottomSheet
import com.zaneschepke.wireguardautotunnel.viewmodel.AppViewModel
import com.zaneschepke.wireguardautotunnel.viewmodel.event.AppEvent

/**
 * Bottom sheet that provides context menu options for manual/custom tunnels.
 * Allows users to delete, rename, or edit their custom endpoint configurations.
 */
@Composable
fun TunnelOptionsBottomSheet(
    isVisible: Boolean,
    tunnel: TunnelConf?,
    onDismiss: () -> Unit,
    onEdit: (TunnelConf) -> Unit,
    viewModel: AppViewModel
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Reset dialog states when sheet is dismissed
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            showRenameDialog = false
            showDeleteConfirmation = false
        }
    }
    
    if (showRenameDialog && tunnel != null) {
        TunnelRenameDialog(
            tunnel = tunnel,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.handleEvent(AppEvent.RenameTunnel(tunnel, newName))
                showRenameDialog = false
                onDismiss()
            }
        )
    }
    
    if (showDeleteConfirmation && tunnel != null) {
        DeleteConfirmationDialog(
            tunnelName = tunnel.tunName,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                viewModel.handleEvent(AppEvent.DeleteTunnel(tunnel))
                showDeleteConfirmation = false
                onDismiss()
            }
        )
    }
    
    GlobalBottomSheet(
        isVisible = isVisible && tunnel != null,
        onDismiss = onDismiss,
        skipPartiallyExpanded = true
    ) {
        tunnel?.let { tunnelConf ->
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Tunnel Options",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = tunnelConf.tunName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Tunnel Options",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Options
            TunnelOptionItem(
                icon = Icons.Outlined.Edit,
                title = "Rename",
                subtitle = "Change the tunnel name",
                onClick = { showRenameDialog = true }
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            TunnelOptionItem(
                icon = Icons.Outlined.Settings,
                title = "Edit Configuration",
                subtitle = "Modify tunnel settings",
                onClick = {
                    onEdit(tunnelConf)
                    onDismiss()
                }
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            TunnelOptionItem(
                icon = Icons.Outlined.Delete,
                title = "Delete",
                subtitle = "Remove this tunnel",
                onClick = { showDeleteConfirmation = true },
                isDestructive = true
            )
        }
    }
}

/**
 * Individual option item in the tunnel options bottom sheet
 */
@Composable
private fun TunnelOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialog for renaming a tunnel
 */
@Composable
private fun TunnelRenameDialog(
    tunnel: TunnelConf,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(tunnel.tunName) }
    var isValid by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Rename Tunnel")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter a new name for the tunnel:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = newName,
                    onValueChange = { 
                        newName = it.trim()
                        isValid = newName.isNotBlank() && newName != tunnel.tunName
                    },
                    label = { Text("Tunnel Name") },
                    singleLine = true,
                    isError = !isValid && newName.isBlank(),
                    supportingText = if (!isValid && newName.isBlank()) {
                        { Text("Name cannot be empty") }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = isValid && newName.isNotBlank() && newName != tunnel.tunName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for confirming tunnel deletion
 */
@Composable
private fun DeleteConfirmationDialog(
    tunnelName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_tunnel))
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$tunnelName\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}