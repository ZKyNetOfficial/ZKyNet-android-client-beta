package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.domain.enums.ConfigType
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.GlobalBottomSheet
import com.zaneschepke.wireguardautotunnel.ui.common.functions.rememberFileExportLauncherForResult
import com.zaneschepke.wireguardautotunnel.ui.navigation.LocalIsAndroidTV
import com.zaneschepke.wireguardautotunnel.ui.screens.settings.components.AuthorizationPromptWrapper
import com.zaneschepke.wireguardautotunnel.ui.state.AppViewState
import com.zaneschepke.wireguardautotunnel.util.Constants
import com.zaneschepke.wireguardautotunnel.util.extensions.hasSAFSupport
import com.zaneschepke.wireguardautotunnel.viewmodel.AppViewModel
import com.zaneschepke.wireguardautotunnel.viewmodel.event.AppEvent

@Composable
fun ExportTunnelsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val isTv = LocalIsAndroidTV.current

    var exportConfigType by remember { mutableStateOf(ConfigType.WG) }
    var showAuthPrompt by remember { mutableStateOf(false) }
    var isAuthorized by remember { mutableStateOf(false) }
    var shouldExport by remember { mutableStateOf(false) }

    val selectedTunnelsExportLauncher =
        rememberFileExportLauncherForResult(
            mimeType = Constants.ZIP_FILE_MIME_TYPE,
            onResult = { file ->
                if (file != null) {
                    viewModel.handleEvent(AppEvent.ExportSelectedTunnels(exportConfigType, file))
                } else {
                    viewModel.handleEvent(AppEvent.ClearSelectedTunnels)
                    onDismiss()
                }
            },
        )

    fun handleFileExport() {
        if (context.hasSAFSupport(Constants.ZIP_FILE_MIME_TYPE)) {
            selectedTunnelsExportLauncher.launch(Constants.DEFAULT_EXPORT_FILE_NAME)
        } else {
            viewModel.handleEvent(AppEvent.ExportSelectedTunnels(exportConfigType, null))
        }
    }

    LaunchedEffect(shouldExport) {
        if (shouldExport) {
            handleFileExport()
            shouldExport = false
        }
    }

    if (showAuthPrompt) {
        AuthorizationPromptWrapper(
            onDismiss = { showAuthPrompt = false },
            onSuccess = {
                showAuthPrompt = false
                isAuthorized = true
                shouldExport = true
            },
            viewModel = viewModel,
        )
    }

    GlobalBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        skipPartiallyExpanded = true
    ) {
        ExportOptionRow(
            label = stringResource(R.string.export_tunnels_amnezia),
            onClick = {
                exportConfigType = ConfigType.AM
                if (!isAuthorized && !isTv) {
                    showAuthPrompt = true
                } else {
                    shouldExport = true
                }
            },
        )
        HorizontalDivider()

        ExportOptionRow(
            label = stringResource(R.string.export_tunnels_wireguard),
            onClick = {
                exportConfigType = ConfigType.WG
                if (!isAuthorized && !isTv) {
                    showAuthPrompt = true
                } else {
                    shouldExport = true
                }
            },
        )
    }
}

@Composable
private fun ExportOptionRow(label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(10.dp)) {
        Icon(
            imageVector = Icons.Filled.FolderZip,
            contentDescription = label,
            modifier = Modifier.padding(10.dp),
        )
        Text(text = label, modifier = Modifier.padding(10.dp))
    }
}
