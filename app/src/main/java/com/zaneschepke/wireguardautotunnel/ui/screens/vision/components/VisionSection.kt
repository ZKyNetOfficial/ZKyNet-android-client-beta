package com.zaneschepke.wireguardautotunnel.ui.screens.vision.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                text = "TORUS aims to make strong, decentralized privacy tools not only accessible but widely adopted. By unifying proven technologies into a seamless experience, we're building a platform that balances user-friendly design, industry-grade scalability, and meaningful rewards for the people who keep the network running. The result? Better privacy for everyone by design, not by sacrifice.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Core Principles",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Expandable Core Principles
            ExpandableVisionItem(
                icon = "🧩",
                title = "Making Privacy Effortless",
                description = "TORUS is built to simplify powerful privacy tools, dynamically balancing usability and protection so everyday users can benefit from advanced features without needing technical expertise."
            )
            
            ExpandableVisionItem(
                icon = "🔒",
                title = "Privacy That Scales",
                description = "Many privacy tools are limited by low adoption and fragmented setups which can inversely give you a more unique online fingerprint. TORUS aims to increase anonymity for all by promoting standardized, high-quality configurations that more users can trust and adopt."
            )
            
            ExpandableVisionItem(
                icon = "🌐",
                title = "Hybrid-First Architecture",
                description = "TORUS is being designed as a flexible network that combines decentralized nodes with scalable infrastructure. This hybrid model supports global reach, high availability, and a more resilient privacy layer ensuring performance without over reliance on centralized systems."
            )
            
            ExpandableVisionItem(
                icon = "💸",
                title = "Aligned Incentives",
                description = "TORUS is being built to reward contributors for powering the network—based on uptime, reliability, and overall value. A free tier helps increase the adoption of new users without undermining the earning potential of node operators, ensuring growth and sustainability go hand in hand."
            )
            
            ExpandableVisionItem(
                icon = "🛠️",
                title = "Modular for the Future",
                description = "The current client offers early users a simple way to show support and preview what's ahead. Dynamic browser features, multi-hop/mesh routing, and ZK-proof integrations are all part of the long-term roadmap."
            )
        }
    }
}

/**
 * Expandable vision item component with emoji icon, title, and collapsible description.
 */
@Composable
private fun ExpandableVisionItem(
    icon: String,
    title: String,
    description: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(24.dp)
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
            }
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 36.dp,
                    bottom = 8.dp,
                    end = 8.dp
                )
            )
        }
    }
}