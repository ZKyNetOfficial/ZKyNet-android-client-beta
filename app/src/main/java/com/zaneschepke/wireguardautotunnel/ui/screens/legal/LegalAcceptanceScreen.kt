package com.zaneschepke.wireguardautotunnel.ui.screens.legal

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.viewmodel.AppViewModel
import com.zaneschepke.wireguardautotunnel.viewmodel.event.AppEvent
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalAcceptanceScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var hasAcceptedTerms by remember { mutableStateOf(false) }
    var hasAcceptedPrivacy by remember { mutableStateOf(false) }
    
    val canAccept = hasAcceptedTerms && hasAcceptedPrivacy
    
    // Store string resources outside the click handlers
    val termsLinkText = stringResource(R.string.legal_terms_link)
    val privacyLinkText = stringResource(R.string.legal_privacy_link)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ZKyNet Logo/Title
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Welcome text
        Text(
            text = stringResource(R.string.legal_welcome_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.legal_welcome_message),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Legal agreements card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.legal_documents_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Terms of Service Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasAcceptedTerms,
                        onCheckedChange = { hasAcceptedTerms = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val termsText = buildAnnotatedString {
                        append("I accept the ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(termsLinkText)
                        }
                    }
                    
                    ClickableText(
                        text = termsText,
                        onClick = { offset ->
                            val termsLinkStart = termsText.text.indexOf(termsLinkText)
                            val termsLinkEnd = termsLinkStart + termsLinkText.length
                            if (offset in termsLinkStart..termsLinkEnd) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zkynet.org/src/legal/terms-mvp.html"))
                                context.startActivity(intent)
                            }
                        },
                        style = LocalTextStyle.current.copy(fontSize = 13.sp),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Privacy Policy Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasAcceptedPrivacy,
                        onCheckedChange = { hasAcceptedPrivacy = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val privacyText = buildAnnotatedString {
                        append("I accept the ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(privacyLinkText)
                        }
                    }
                    
                    ClickableText(
                        text = privacyText,
                        onClick = { offset ->
                            val privacyLinkStart = privacyText.text.indexOf(privacyLinkText)
                            val privacyLinkEnd = privacyLinkStart + privacyLinkText.length
                            if (offset in privacyLinkStart..privacyLinkEnd) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zkynet.org/src/legal/privacy-policy.html"))
                                context.startActivity(intent)
                            }
                        },
                        style = LocalTextStyle.current.copy(fontSize = 13.sp),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Agreement text
        Text(
            text = stringResource(R.string.legal_agreement_text),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Accept button (centered and above decline)
        Button(
            onClick = {
                viewModel.handleEvent(AppEvent.AcceptLegalTerms)
            },
            enabled = canAccept,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(stringResource(R.string.legal_accept))
        }
        
        // Decline button
        OutlinedButton(
            onClick = {
                exitProcess(0)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.legal_decline))
        }
        
        if (!canAccept) {
            Text(
                text = stringResource(R.string.legal_must_accept_both),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}