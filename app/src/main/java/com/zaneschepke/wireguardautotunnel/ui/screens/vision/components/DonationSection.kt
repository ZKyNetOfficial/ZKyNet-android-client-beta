package com.zaneschepke.wireguardautotunnel.ui.screens.vision.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Component for displaying donation information and call-to-action.
 * Explains how user donations support the TORUS VPN project and mission.
 */
@Composable
fun DonationSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.VolunteerActivism,
                contentDescription = "Support",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Support Our Mission",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Help us build a better, more secure internet for everyone. Your support enables us to:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DonationBenefit("• Maintain and improve server infrastructure")
                DonationBenefit("• Develop new privacy-focused features")
                DonationBenefit("• Keep the service accessible and affordable")
                DonationBenefit("• Support open-source development")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    // TODO: Implement donation functionality
                    // This can be connected to payment processor or donation platform
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = "Donate",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Support TORUS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            Text(
                text = "Every contribution, no matter the size, makes a difference in protecting digital privacy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Individual donation benefit item.
 */
@Composable
private fun DonationBenefit(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    )
}