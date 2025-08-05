/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.data.model.TorusServerConfig

/**
 * Component displaying a list of TORUS VPN servers with real configuration data.
 * Shows available server locations with connection capabilities.
 */
@Composable
fun TorusServerList(
    servers: List<TorusServerConfig>,
    onConnectToServer: (TorusServerConfig) -> Unit,
    connectedServerId: String? = null,
    onCustomEndpointClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "TORUS Servers",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(servers) { server ->
            val serverDisplayInfo = ServerDisplayInfo(
                id = server.id,
                displayName = server.displayName,
                location = server.location,
                country = server.country,
                serverType = if (server.isTestServer) ServerType.TORUS_TEST else ServerType.TORUS_REGULAR,
                connectionStatus = if (server.id == connectedServerId) ConnectionStatus.CONNECTED else ConnectionStatus.AVAILABLE
            )
            
            ServerItemCard(
                server = serverDisplayInfo,
                onConnect = { onConnectToServer(server) }
            )
        }
        
        // Custom Endpoint button at the bottom
        item {
            val customEndpointDisplayInfo = ServerDisplayInfo(
                id = "custom_endpoint",
                displayName = "Custom Endpoint",
                location = "Import your own VPN configuration",
                country = "",
                serverType = ServerType.MANUAL,
                connectionStatus = ConnectionStatus.AVAILABLE
            )
            
            ServerItemCard(
                server = customEndpointDisplayInfo,
                onConnect = onCustomEndpointClick
            )
        }
    }
}

