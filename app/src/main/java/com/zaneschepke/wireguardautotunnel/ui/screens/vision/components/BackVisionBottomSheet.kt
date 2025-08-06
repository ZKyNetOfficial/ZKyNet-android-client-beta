/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.vision.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.data.network.UserSupportApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackVisionBottomSheet(
    onDismiss: () -> Unit,
    userSupportApi: UserSupportApi
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasSubmittedSignal by remember { mutableStateOf(false) }
    var emailSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "Back the Vision",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Show your support by sending an anonymous signal that you agree with TORUS's vision for privacy and decentralization.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "What this does:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Sends one anonymous support signal per user",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Helps us gauge community interest",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• No personal information is sent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (!hasSubmittedSignal) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = ""
                            
                            userSupportApi.sendSupportSignal()
                                .onSuccess {
                                    hasSubmittedSignal = true
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "Failed to send signal"
                                }
                            
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Send Support",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Support Signal")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Support signal sent! Thank you for backing the vision.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            HorizontalDivider()
            
            Text(
                text = "Want updates on our progress?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            if (!emailSubmitted) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = ""
                            
                            userSupportApi.submitEmailForUpdates(email)
                                .onSuccess {
                                    emailSubmitted = true
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "Failed to submit email"
                                }
                            
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Send Email",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Email for Updates")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Email submitted! You'll receive updates on our progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}