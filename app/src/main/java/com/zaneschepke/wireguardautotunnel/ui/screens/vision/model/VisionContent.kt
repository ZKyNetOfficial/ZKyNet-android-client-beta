/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.screens.vision.model

/**
 * Data model for Vision page content to separate content from UI components.
 * Enables easy content updates without modifying UI code.
 */
data class VisionContent(
    val aboutTitle: String,
    val aboutParagraphs: List<String>,
    val principlesTitle: String,
    val principles: List<PrincipleItem>
)

/**
 * Individual principle item with icon, title, and expandable description.
 */
data class PrincipleItem(
    val icon: String,
    val title: String,
    val description: String
)

/**
 * Content provider for Vision page data.
 * Currently provides static content but can be extended for CMS/API integration.
 */
object VisionContentProvider {
    
    fun getVisionContent(): VisionContent {
        return VisionContent(
            aboutTitle = "About TORUS",
            aboutParagraphs = listOf(
                "TORUS aims to make strong, decentralized privacy tools not only accessible but widely adopted.",
                "By unifying proven technologies into a seamless experience, we're building a platform that balances user-friendly design, industry-grade scalability, and meaningful rewards for the people who keep the network running.",
                "The result? Better privacy for everyone by design, not by sacrifice."
            ),
            principlesTitle = "Core Principles",
            principles = listOf(
                PrincipleItem(
                    icon = "🧩",
                    title = "Making Privacy Effortless",
                    description = "TORUS is built to simplify powerful privacy tools, dynamically balancing usability and protection so everyday users can benefit from advanced features without needing technical expertise."
                ),
                PrincipleItem(
                    icon = "🔒",
                    title = "Privacy That Scales",
                    description = "Many privacy tools are limited by low adoption and fragmented setups which can inversely give you a more unique online fingerprint. TORUS aims to increase anonymity for all by promoting standardized, high-quality configurations that more users can trust and adopt."
                ),
                PrincipleItem(
                    icon = "🌐",
                    title = "Hybrid-First Architecture",
                    description = "TORUS is being designed as a flexible network that combines decentralized nodes with scalable infrastructure. This hybrid model supports global reach, high availability, and a more resilient privacy layer ensuring performance without over reliance on centralized systems."
                ),
                PrincipleItem(
                    icon = "💸",
                    title = "Aligned Incentives",
                    description = "TORUS is being built to reward contributors for powering the network—based on uptime, reliability, and overall value. A free tier helps increase the adoption of new users without undermining the earning potential of node operators, ensuring growth and sustainability go hand in hand."
                ),
                PrincipleItem(
                    icon = "🛠️",
                    title = "Modular for the Future",
                    description = "The current client offers early users a simple way to show support and preview what's ahead. Dynamic browser features, multi-hop/mesh routing, and ZK-proof integrations are all part of the long-term roadmap."
                )
            )
        )
    }
}