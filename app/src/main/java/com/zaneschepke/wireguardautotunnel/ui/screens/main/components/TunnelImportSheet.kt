package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.GlobalBottomSheet
import com.zaneschepke.wireguardautotunnel.ui.navigation.LocalIsAndroidTV

@Composable
fun TunnelImportSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFileClick: () -> Unit,
    onQrClick: () -> Unit,
    onManualImportClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onUrlClick: () -> Unit,
) {
    val isTv = LocalIsAndroidTV.current
    var showHelp by remember { mutableStateOf(false) }

    GlobalBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        skipPartiallyExpanded = true
    ) {
            // Header with help toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Endpoint",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = { showHelp = !showHelp }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = if (showHelp) "Hide help" else "Show help",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            if (showHelp) {
                CustomEndpointHelpContent()
            } else {
                CustomEndpointImportOptions(
                    isTv = isTv,
                    onDismiss = onDismiss,
                    onFileClick = onFileClick,
                    onQrClick = onQrClick,
                    onClipboardClick = onClipboardClick,
                    onManualImportClick = onManualImportClick,
                    onUrlClick = onUrlClick
                )
            }
    }
}

@Composable
private fun CustomEndpointHelpContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Privacy Benefits Section
        Text(
            text = "Privacy Benefits",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Having extra VPN endpoints is a significant privacy benefit. By using multiple VPN providers and servers, you can:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BulletPoint("Avoid dependency on a single VPN provider")
            BulletPoint("Switch between different server locations for better privacy")
            BulletPoint("Use specialized servers for different activities")
            BulletPoint("Have backup options if one service is unavailable")
        }
        
        HorizontalDivider()
        
        // What is a Custom Endpoint Section
        Text(
            text = "What is a Custom Endpoint?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "A custom endpoint allows you to import VPN configuration files (.conf files) from other VPN providers. These files contain all the necessary connection details to establish a secure VPN tunnel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        HorizontalDivider()
        
        // Example Provider Section
        Text(
            text = "Example: Free VPN Configurations",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "ProtonVPN offers free VPN configurations that you can download and import. This is just one example - many VPN providers offer configuration files for manual setup.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Note: ProtonVPN Example",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "We mention ProtonVPN as an example only. We do not endorse any specific VPN provider. Always research and choose VPN services that meet your specific privacy and security needs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CustomEndpointImportOptions(
    isTv: Boolean,
    onDismiss: () -> Unit,
    onFileClick: () -> Unit,
    onQrClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onManualImportClick: () -> Unit,
    onUrlClick: () -> Unit
) {
    Column {
        ImportOption(
            icon = Icons.Filled.FileOpen,
            title = stringResource(id = R.string.add_tunnels_text),
            onClick = {
                onDismiss()
                onFileClick()
            }
        )
        
        if (!isTv) {
            ImportOption(
                icon = Icons.Filled.QrCode,
                title = stringResource(id = R.string.add_from_qr),
                onClick = {
                    onDismiss()
                    onQrClick()
                }
            )
            
            ImportOption(
                icon = Icons.Filled.ContentPasteGo,
                title = stringResource(id = R.string.add_from_clipboard),
                onClick = {
                    onDismiss()
                    onClipboardClick()
                }
            )
        }
        
        ImportOption(
            icon = Icons.Filled.Link,
            title = stringResource(id = R.string.add_from_url),
            onClick = {
                onDismiss()
                onUrlClick()
            }
        )
        
        ImportOption(
            icon = Icons.Filled.Create,
            title = stringResource(id = R.string.create_import),
            onClick = {
                onDismiss()
                onManualImportClick()
            },
            isLast = true
        )
    }
}

@Composable
private fun ImportOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (!isLast) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}