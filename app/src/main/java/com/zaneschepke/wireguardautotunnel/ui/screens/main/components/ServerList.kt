package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.domain.model.ZKyNetServer

/**
 * Component displaying a list of hardcoded ZKyNet VPN servers.
 * Shows available server locations with connection status and latency information.
 */
@Composable
fun ServerList(
    onConnectToServer: (ZKyNetServer) -> Unit,
    connectedServerId: String? = null,
    modifier: Modifier = Modifier
) {
    val servers = remember { getHardcodedZKyNetServers() }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "ZKyNet Servers",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(servers) { server ->
            ServerItem(
                server = server,
                isConnected = server.id == connectedServerId,
                onConnect = { onConnectToServer(server) }
            )
        }
    }
}

/**
 * Individual server item component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerItem(
    server: ZKyNetServer,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    val clickAction: () -> Unit = if (!isConnected && server.isOnline) onConnect else fun() { }
    Card(
        onClick = clickAction,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isConnected -> MaterialTheme.colorScheme.primaryContainer
                !server.isOnline -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
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
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Server Location",
                    tint = if (isConnected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Column {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isConnected) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${server.location}, ${server.country}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isConnected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Connection Status
                Text(
                    text = when {
                        isConnected -> "Connected"
                        !server.isOnline -> "Offline"
                        else -> "Available"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        isConnected -> MaterialTheme.colorScheme.primary
                        !server.isOnline -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // Latency display
                server.latency?.let { latency ->
                    Text(
                        text = "${latency}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Returns a list of hardcoded ZKyNet VPN servers.
 * In a production app, this would likely come from a backend API.
 */
private fun getHardcodedZKyNetServers(): List<ZKyNetServer> {
    return listOf(
ZKyNetServer(
            id = "us-east-1",
            name = "New York",
            location = "New York",
            country = "United States",
            countryCode = "US",
            endpoint = "ny.torus-vpn.com:51820",
            publicKey = "placeholder-public-key-ny",
            isOnline = true,
            latency = 25
        ),
ZKyNetServer(
            id = "us-west-1",
            name = "Los Angeles",
            location = "Los Angeles",
            country = "United States",
            countryCode = "US",
            endpoint = "la.torus-vpn.com:51820",
            publicKey = "placeholder-public-key-la",
            isOnline = true,
            latency = 45
        ),
ZKyNetServer(
            id = "eu-west-1",
            name = "London",
            location = "London",
            country = "United Kingdom",
            countryCode = "GB",
            endpoint = "london.torus-vpn.com:51820",
            publicKey = "placeholder-public-key-london",
            isOnline = true,
            latency = 78
        ),
ZKyNetServer(
            id = "eu-central-1",
            name = "Frankfurt",
            location = "Frankfurt",
            country = "Germany",
            countryCode = "DE",
            endpoint = "frankfurt.torus-vpn.com:51820",
            publicKey = "placeholder-public-key-frankfurt",
            isOnline = true,
            latency = 65
        ),
ZKyNetServer(
            id = "asia-east-1",
            name = "Tokyo",
            location = "Tokyo",
            country = "Japan",
            countryCode = "JP",
            endpoint = "tokyo.torus-vpn.com:51820",
            publicKey = "placeholder-public-key-tokyo",
            isOnline = false, // Example of offline server
            latency = 120
        )
    )
}