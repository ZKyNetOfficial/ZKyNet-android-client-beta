package com.zaneschepke.wireguardautotunnel.ui.screens.settings.logs.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.common.bottomsheet.GlobalBottomSheet
import com.zaneschepke.wireguardautotunnel.ui.state.AppViewState
import com.zaneschepke.wireguardautotunnel.viewmodel.AppViewModel
import com.zaneschepke.wireguardautotunnel.viewmodel.event.AppEvent

@Composable
fun LogsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AppViewModel
) {
    GlobalBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        skipPartiallyExpanded = true
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable {
                        onDismiss()
                        viewModel.handleEvent(AppEvent.ExportLogs)
                    }
                    .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FolderZip,
                contentDescription = stringResource(R.string.export_logs),
                modifier = Modifier.padding(10.dp),
            )
            Text(text = stringResource(R.string.export_logs), modifier = Modifier.padding(10.dp))
        }
        HorizontalDivider()
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable {
                        onDismiss()
                        viewModel.handleEvent(AppEvent.DeleteLogs)
                    }
                    .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_logs),
                modifier = Modifier.padding(10.dp),
            )
            Text(text = stringResource(R.string.delete_logs), modifier = Modifier.padding(10.dp))
        }
    }
}
