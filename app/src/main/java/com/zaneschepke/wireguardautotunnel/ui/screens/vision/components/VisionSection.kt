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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.ui.screens.vision.model.PrincipleItem
import com.zaneschepke.wireguardautotunnel.ui.screens.vision.model.VisionContentProvider
import com.zaneschepke.wireguardautotunnel.ui.screens.vision.theme.VisionDefaults

/**
 * Component displaying TORUS company vision and mission statement.
 * Presents core values and goals related to privacy, security, and internet freedom.
 */
@Composable
fun VisionSection() {
    val content = VisionContentProvider.getVisionContent()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(VisionDefaults.sectionPadding),
            verticalArrangement = Arrangement.spacedBy(VisionDefaults.sectionSpacing)
        ) {
            CenteredSectionTitle(text = content.aboutTitle)
            
            AboutContentCard(paragraphs = content.aboutParagraphs)
            
            Spacer(modifier = Modifier.height(VisionDefaults.afterAboutSpacing))
            
            CenteredSectionTitle(text = content.principlesTitle)
            
            Spacer(modifier = Modifier.height(VisionDefaults.beforePrinciplesSpacing))
            
            PrinciplesList(principles = content.principles)
        }
    }
}

/**
 * Reusable centered section title component.
 */
@Composable
private fun CenteredSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Reusable about content card with multiple paragraphs.
 */
@Composable
private fun AboutContentCard(paragraphs: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = VisionDefaults.aboutCardAlpha)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(VisionDefaults.aboutCardPadding),
            verticalArrangement = Arrangement.spacedBy(VisionDefaults.paragraphSpacing)
        ) {
            paragraphs.forEachIndexed { index, paragraph ->
                val isLastParagraph = index == paragraphs.lastIndex
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isLastParagraph) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isLastParagraph) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Reusable principles list component.
 */
@Composable
private fun PrinciplesList(principles: List<PrincipleItem>) {
    Column {
        principles.forEach { principle ->
            ExpandableVisionItem(
                icon = principle.icon,
                title = principle.title,
                description = principle.description
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
                .padding(vertical = VisionDefaults.expandableCardPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(VisionDefaults.expandableIconSize)
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(VisionDefaults.expandIconSize)
            )
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = VisionDefaults.expandedContentStartPadding,
                    bottom = VisionDefaults.expandedContentBottomPadding,
                    end = VisionDefaults.expandedContentEndPadding
                )
            )
        }
    }
}