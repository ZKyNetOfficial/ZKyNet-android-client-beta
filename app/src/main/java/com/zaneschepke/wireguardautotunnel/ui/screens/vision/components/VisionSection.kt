package com.zaneschepke.wireguardautotunnel.ui.screens.vision.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Component displaying TORUS company vision and mission statement.
 * Presents core values and goals related to privacy, security, and internet freedom.
 */
@Composable
fun VisionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "About TORUS",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "TORUS is dedicated to providing secure, private, and reliable VPN services that protect your digital rights and online freedom.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Core Values
            VisionItem(
                icon = Icons.Outlined.Security,
                title = "Privacy First",
                description = "Your data belongs to you. We use cutting-edge encryption to ensure your online activities remain private."
            )
            
            VisionItem(
                icon = Icons.Outlined.Visibility,
                title = "Transparency",
                description = "Open source approach and clear policies. No hidden agendas, no data collection."
            )
            
            VisionItem(
                icon = Icons.Outlined.Public,
                title = "Global Access",
                description = "Breaking down digital barriers and providing secure internet access worldwide."
            )
        }
    }
}

/**
 * Individual vision item component displaying an icon, title, and description.
 */
@Composable
private fun VisionItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}