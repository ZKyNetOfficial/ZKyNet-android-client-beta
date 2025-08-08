/*
 * ZKyNet VPN - Custom VPN Client
 * Copyright (c) 2025 ZKyNet
 * 
 * This file is part of ZKyNet VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet

import androidx.compose.runtime.*

/**
 * State holder for managing bottom sheet visibility and type.
 * Provides a clean API for showing/hiding different bottom sheets.
 */
@Stable
class BottomSheetState {
    internal var _currentSheet by mutableStateOf<BottomSheetType?>(null)
    
    val currentSheet: BottomSheetType? by derivedStateOf { _currentSheet }
    val isVisible: Boolean by derivedStateOf { _currentSheet != null }
    
    fun show(sheetType: BottomSheetType) {
        _currentSheet = sheetType
    }
    
    fun hide() {
        _currentSheet = null
    }
    
    inline fun <reified T : BottomSheetType> isShowing(): Boolean {
        return currentSheet is T
    }
}

/**
 * Sealed class for defining different types of bottom sheets.
 * Add new types here as needed.
 */
sealed class BottomSheetType {
    object Donate : BottomSheetType()
    object BackVision : BottomSheetType()
    object NodeOperator : BottomSheetType()
}

/**
 * Composable function to remember a BottomSheetState.
 */
@Composable
fun rememberBottomSheetState(): BottomSheetState {
    return remember { BottomSheetState() }
}