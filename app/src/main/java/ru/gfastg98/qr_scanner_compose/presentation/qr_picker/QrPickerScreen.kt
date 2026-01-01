package ru.gfastg98.qr_scanner_compose.presentation.qr_picker

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.presentation.ObserveAsEvents
import ru.gfastg98.qr_scanner_compose.presentation.components.Screen
import ru.gfastg98.qr_scanner_compose.presentation.components.dialog.LoadingDialog
import ru.gfastg98.qr_scanner_compose.presentation.utils.drawBarcodes

@Composable
fun QrPickerScreen() {
    val activity = LocalActivity.current
    activity ?: return

    val viewModel: QrPickerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SideEffect { viewModel.initializeWith(activity) }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            QrPickerScreenEvent.Finish -> activity.finish()
        }
    }

    QrPickerScreenRoot(state, viewModel::onAction)
}

@Composable
private fun QrPickerScreenRoot(
    state: QrPickerScreenState,
    onAction: (QrPickerScreenAction) -> Unit,
) = Screen {
    val activity = LocalActivity.current
    activity ?: return@Screen

    title = "QR Code Picker"

    navigationIconAction {
        activity.finish()
    }

    content {
        val bitmap = state.bitmap
        val barcodes = state.barcodes
        if (barcodes?.isNotEmpty() == true && bitmap != null) {
            Column {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .clip(MaterialTheme.shapes.large),
                    bitmap = bitmap.drawBarcodes(barcodes, state.selected).asImageBitmap(),
                    contentDescription = null
                )
                Column {
                    Row {
                        IconButton(
                            enabled = state.selected > 0,
                            modifier = Modifier.weight(0.5f),
                            onClick = { onAction(QrPickerScreenAction.Previous) }
                        ) {
                            Icon(Icons.Default.ChevronLeft, null)
                        }
                        IconButton(
                            enabled = state.selected < barcodes.size - 1,
                            modifier = Modifier.weight(0.5f),
                            onClick = { onAction(QrPickerScreenAction.Next) }
                        ) {
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10),
                        onClick = { onAction(QrPickerScreenAction.Save) }
                    ) {
                        Text("Сохранить выбранное и закрыть")
                    }
                }
            }
        }

        if (!state.isReady()) {
            LoadingDialog()
        }
    }
}

