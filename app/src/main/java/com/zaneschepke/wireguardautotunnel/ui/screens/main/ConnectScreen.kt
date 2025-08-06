/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.core.tunnel.getValueById
import com.zaneschepke.wireguardautotunnel.data.model.ZKyNetServerConfig
import com.zaneschepke.wireguardautotunnel.data.service.DynamicServerConfigManager
import com.zaneschepke.wireguardautotunnel.domain.model.TunnelConf
import com.zaneschepke.wireguardautotunnel.domain.state.TunnelState
import com.zaneschepke.wireguardautotunnel.ui.Route
import com.zaneschepke.wireguardautotunnel.ui.common.dialog.InfoDialog
import com.zaneschepke.wireguardautotunnel.ui.common.functions.rememberClipboardHelper
import com.zaneschepke.wireguardautotunnel.ui.common.functions.rememberFileImportLauncherForResult
import com.zaneschepke.wireguardautotunnel.ui.navigation.LocalNavController
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.ExportTunnelsBottomSheet
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.ServerDisplayInfo
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.ServerItemCard
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.ServerType
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.ConnectionStatus
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.ZKyNetServerList
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.TunnelImportSheet
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.UrlImportDialog
import com.zaneschepke.wireguardautotunnel.ui.state.AppUiState
import com.zaneschepke.wireguardautotunnel.ui.state.AppViewState
import com.zaneschepke.wireguardautotunnel.util.Constants
import com.zaneschepke.wireguardautotunnel.util.StringValue
import com.zaneschepke.wireguardautotunnel.viewmodel.AppViewModel
import com.zaneschepke.wireguardautotunnel.viewmodel.event.AppEvent

/**
 * Main connect screen for ZKyNet VPN showing available servers.
 * Simplified interface focused on connecting to predefined ZKyNet servers
 * rather than managing custom tunnel configurations.
 */
@Composable
fun ConnectScreen(
    appUiState: AppUiState,
    appViewState: AppViewState,
    viewModel: AppViewModel,
    configManager: DynamicServerConfigManager
) {
    val navController = LocalNavController.current
    val clipboard = rememberClipboardHelper()
    
    // Tunnel import functionality state
    var showUrlImportDialog by remember { mutableStateOf(false) }
    
    // File import launcher
    val tunnelFileImportResultLauncher =
        rememberFileImportLauncherForResult(
            onNoFileExplorer = {
                viewModel.handleEvent(
                    AppEvent.ShowMessage(
                        StringValue.StringResource(R.string.error_no_file_explorer)
                    )
                )
            },
            onData = { data -> viewModel.handleEvent(AppEvent.ImportTunnelFromFile(data)) },
        )
    
    // QR code scanner
    val scanLauncher =
        rememberLauncherForActivityResult(
            contract = ScanContract(),
            onResult = { result ->
                if (result != null && result.contents.isNotEmpty())
                    viewModel.handleEvent(AppEvent.ImportTunnelFromQrCode(result.contents))
            },
        )
    
    // Camera permission launcher
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted
            ->
            if (!isGranted) {
                viewModel.handleEvent(
                    AppEvent.ShowMessage(
                        StringValue.StringResource(R.string.camera_permission_required)
                    )
                )
                return@rememberLauncherForActivityResult
            }
            scanLauncher.launch(
                ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setBeepEnabled(false)
            )
        }
    
    // Collect server configurations using the new state pattern
    val configurationState by configManager.configurationState.collectAsState()
    
    // Check if any ZKyNet tunnels are currently active
    val activeTunnels = appUiState.activeTunnels
    val zkynetActiveTunnel = activeTunnels.keys.find { it.tunName.startsWith("ZKyNet ") }
    val connectedServerId = zkynetActiveTunnel?.let { tunnel ->
        when (val state = configurationState) {
            is DynamicServerConfigManager.ConfigurationState.Success -> {
                state.servers.find { server -> 
                    tunnel.tunName == "ZKyNet ${server.displayName}"
                }?.id
            }
            else -> null
        }
    }
    
    // Handle delete dialog
    if (appViewState.showModal == AppViewState.ModalType.DELETE) {
        InfoDialog(
            onDismiss = {
                viewModel.handleEvent(AppEvent.SetShowModal(AppViewState.ModalType.NONE))
            },
            onAttest = {
                viewModel.handleEvent(AppEvent.DeleteSelectedTunnels)
                viewModel.handleEvent(AppEvent.SetShowModal(AppViewState.ModalType.NONE))
            },
            title = { Text(text = stringResource(R.string.delete_tunnel)) },
            body = { Text(text = stringResource(R.string.delete_tunnel_message)) },
            confirmText = { Text(text = stringResource(R.string.yes)) },
        )
    }
    
    // Handle bottom sheets
    when (appViewState.bottomSheet) {
        AppViewState.BottomSheet.EXPORT_TUNNELS -> {
            ExportTunnelsBottomSheet(viewModel)
        }
        AppViewState.BottomSheet.IMPORT_TUNNELS -> {
            TunnelImportSheet(
                onDismiss = {
                    viewModel.handleEvent(AppEvent.SetBottomSheet(AppViewState.BottomSheet.NONE))
                },
                onFileClick = {
                    tunnelFileImportResultLauncher.launch(Constants.ALLOWED_TV_FILE_TYPES)
                },
                onQrClick = {
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                },
                onClipboardClick = {
                    clipboard.paste { result ->
                        if (result != null)
                            viewModel.handleEvent(AppEvent.ImportTunnelFromClipboard(result))
                    }
                },
                onManualImportClick = {
                    navController.navigate(Route.Config(Constants.MANUAL_TUNNEL_CONFIG_ID))
                },
                onUrlClick = { showUrlImportDialog = true },
            )
        }
        else -> Unit
    }
    
    // Handle URL import dialog
    if (showUrlImportDialog) {
        UrlImportDialog(
            onDismiss = { showUrlImportDialog = false },
            onConfirm = { url ->
                viewModel.handleEvent(AppEvent.ImportTunnelFromUrl(url))
                showUrlImportDialog = false
            },
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection Status Header
        ConnectionStatusCard(
            isConnected = zkynetActiveTunnel != null,
            connectedServerName = zkynetActiveTunnel?.let { tunnel ->
                // Extract server name from tunnel name (remove "ZKyNet " prefix)
                tunnel.tunName.removePrefix("ZKyNet ")
            },
            onDisconnect = { 
                // Disconnect the active ZKyNet tunnel
                zkynetActiveTunnel?.let { tunnel ->
                    viewModel.handleEvent(AppEvent.StopTunnel(tunnel))
                }
            }
        )
        
        // Manual Tunnels Section
        ManualTunnelsSection(
            appUiState = appUiState,
            onConnectToTunnel = { tunnel ->
                // Disconnect any existing tunnel before connecting
                val activeTunnel = appUiState.activeTunnels.keys.firstOrNull()
                activeTunnel?.let {
                    viewModel.handleEvent(AppEvent.StopTunnel(it))
                }
                // Connect to manual tunnel
                viewModel.handleEvent(AppEvent.StartTunnel(tunnel))
            },
            onEditTunnel = { tunnel ->
                navController.navigate(Route.Config(tunnel.id))
            },
            onRetryConnection = { tunnel ->
                viewModel.handleEvent(AppEvent.StartTunnel(tunnel))
            }
        )
        
        // Handle different configuration states
        when (val state = configurationState) {
            is DynamicServerConfigManager.ConfigurationState.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Loading server configurations...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            is DynamicServerConfigManager.ConfigurationState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Configuration Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { viewModel.handleEvent(AppEvent.ReloadServerConfigs) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            is DynamicServerConfigManager.ConfigurationState.Empty -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Server Configurations Found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Please check your configuration file",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            is DynamicServerConfigManager.ConfigurationState.Success -> {
                // ZKyNet Server List
                ZKyNetServerList(
                    servers = state.servers,
                    onConnectToServer = { server ->
                        if (connectedServerId != server.id) {
                            // Disconnect any existing tunnel before connecting to new server
                            val activeTunnel = appUiState.activeTunnels.keys.firstOrNull()
                            activeTunnel?.let { tunnel ->
                                viewModel.handleEvent(AppEvent.StopTunnel(tunnel))
                            }
                            // Connect to ZKyNet server using the AppViewModel
                            viewModel.handleEvent(AppEvent.ConnectToZKyNetServer(server))
                        }
                    },
                    onCustomEndpointClick = {
                        // Show the tunnel import bottom sheet
                        viewModel.handleEvent(AppEvent.SetBottomSheet(AppViewState.BottomSheet.IMPORT_TUNNELS))
                    },
                    connectedServerId = connectedServerId,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Connection status card showing current VPN status.
 */
@Composable
private fun ConnectionStatusCard(
    isConnected: Boolean,
    connectedServerName: String? = null,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = "VPN Status",
                modifier = Modifier.size(40.dp),
                tint = if (isConnected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isConnected) 
                    MaterialTheme.colorScheme.onPrimaryContainer
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isConnected && connectedServerName != null) {
                Text(
                    text = "Connected to $connectedServerName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Disconnect")
                }
            } else {
                Text(
                    text = "Select a server below to connect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Manual tunnels section showing imported .conf files.
 * Displays manual tunnels at the top of the server list with tertiary color scheme.
 */
@Composable
private fun ManualTunnelsSection(
    appUiState: AppUiState,
    onConnectToTunnel: (TunnelConf) -> Unit,
    onEditTunnel: (TunnelConf) -> Unit,
    onRetryConnection: (TunnelConf) -> Unit
) {
    // Filter manual tunnels (non-ZKyNet tunnels)
    val manualTunnels = appUiState.tunnels.filter { tunnel ->
        !tunnel.tunName.startsWith("ZKyNet ")
    }
    
    if (manualTunnels.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Section header
            Text(
                text = "Manual Configurations",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Manual tunnel items
            manualTunnels.forEach { tunnel ->
                val tunnelState = appUiState.activeTunnels.getValueById(tunnel.id)
                val isInActiveMap = appUiState.activeTunnels.containsKey(tunnel)
                
                // Enhanced connection status monitoring with proper state handling
                val connectionStatus = when {
                    // Tunnel is actively connected and up
                    tunnelState?.status?.isUp() == true -> ConnectionStatus.CONNECTED
                    
                    // Tunnel is starting/connecting
                    tunnelState?.status is com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.Starting -> ConnectionStatus.CONNECTING
                    
                    // Tunnel is in the active map but down - indicates a failure
                    isInActiveMap && (tunnelState?.status?.isDown() == true) -> ConnectionStatus.FAILED
                    
                    // Tunnel is stopping - show as disconnecting
                    tunnelState?.status is com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.Stopping -> {
                        val stopping = tunnelState.status as com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.Stopping
                        when (stopping.reason) {
                            com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.StopReason.CONFIG_CHANGED -> ConnectionStatus.CONFIG_ERROR
                            com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.StopReason.PING -> ConnectionStatus.NETWORK_ERROR
                            else -> ConnectionStatus.AVAILABLE
                        }
                    }
                    
                    // Check backend state for additional error conditions
                    tunnelState?.backendState == com.zaneschepke.wireguardautotunnel.domain.enums.BackendState.INACTIVE && isInActiveMap -> ConnectionStatus.FAILED
                    
                    // Default to available for manual tunnels not in active state
                    else -> ConnectionStatus.AVAILABLE
                }
                
                // Enhanced error messages with specific guidance
                val errorMessage = when (connectionStatus) {
                    ConnectionStatus.FAILED -> {
                        if (tunnelState?.status is com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.Stopping) {
                            val stopping = tunnelState.status as com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.Stopping
                            when (stopping.reason) {
                                com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.StopReason.PING -> "Connection lost (ping timeout)"
                                com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.StopReason.CONFIG_CHANGED -> "Configuration changed"
                                else -> "Connection failed"
                            }
                        } else {
                            "Connection failed - check network settings"
                        }
                    }
                    ConnectionStatus.CONFIG_ERROR -> "Invalid tunnel configuration - please edit"
                    ConnectionStatus.NETWORK_ERROR -> "Network error - check connectivity"
                    else -> null
                }
                
                val serverDisplayInfo = ServerDisplayInfo(
                    id = tunnel.id.toString(),
                    displayName = tunnel.tunName,
                    location = "Manual Import",
                    country = "",
                    serverType = ServerType.MANUAL,
                    connectionStatus = connectionStatus,
                    errorMessage = errorMessage
                )
                
                ServerItemCard(
                    server = serverDisplayInfo,
                    onConnect = { onConnectToTunnel(tunnel) },
                    onRetry = { onRetryConnection(tunnel) },
                    onEdit = { onEditTunnel(tunnel) }
                )
            }
            
            // Add spacing between manual and ZKyNet sections
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}