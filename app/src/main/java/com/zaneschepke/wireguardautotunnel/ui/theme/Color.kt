package com.zaneschepke.wireguardautotunnel.ui.theme

import androidx.compose.ui.graphics.Color

val OffWhite = Color(0xFFF2F2F4)
val CoolGray = Color(0xFF8D9D9F)
val LightGrey = Color(0xFFECEDEF)
val TorusOrange = Color(0xFFFF7D4C) // Primary orange
val TorusOrangeDark = Color(0xFFE85A2B) // Darker orange for accent
val TorusOrangeLight = Color(0xFFFF9970) // Lighter orange
val DarkCharcoal = Color(0xFF2C2C2E) // Dark background
val Shark = Color(0xFF21272A)
val BalticSea = Color(0xFF1C1B1F)

// amoled
val ElectricOrange = Color(0xFFFF8A50)

// Status colors
val SilverTree = Color(0xFF6DB58B)
val Brick = Color(0xFFCE4257)
val Straw = Color(0xFFD4C483)

sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val onSurface: Color,
    val onBackground: Color,
    val outline: Color,
) {

    data object Light :
        ThemeColors(
            background = LightGrey.copy(alpha = 0.95f),
            surface = OffWhite,
            primary = TorusOrange,
            secondary = LightGrey,
            onSurface = BalticSea,
            outline = TorusOrangeDark.copy(alpha = .75f),
            onBackground = BalticSea,
        )

    data object Dark :
        ThemeColors(
            background = BalticSea,
            surface = Shark,
            primary = TorusOrange,
            secondary = DarkCharcoal,
            onSurface = OffWhite,
            outline = CoolGray,
            onBackground = OffWhite,
        )
}
