package ru.gfastg98.qr_scanner_compose.presentation.qr_result

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.common.Barcode
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.gfastg98.qr_scanner_compose.R
import ru.gfastg98.qr_scanner_compose.presentation.ObserveAsEvents
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeTypeIcon
import ru.gfastg98.qr_scanner_compose.presentation.components.Screen

@Composable
fun QrResultScreen() {
    val activity = LocalActivity.current
    val intent = activity?.intent ?: return
    val viewModel: QrResultViewModel = koinViewModel(parameters = { parametersOf(intent) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            QrResultEvent.Finish -> activity.finish()
        }
    }

    QrResultScreenRoot(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun QrResultScreenRoot(
    state: QrResultState,
    onAction: (QrResultAction) -> Unit,
) = Screen {
    val activity = LocalActivity.current
    LocalContext.current
    val intent = activity?.intent ?: return@Screen

    val isForView = remember { intent.getBooleanExtra("view", false) }
    val type = remember { intent.getIntExtra(QrResultActivity.EXTRA_CODE_FORMAT, 0) }

    title = "QR-код"

    actions {
        var expanded by remember { mutableStateOf(false) }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Image, null) },
                text = { Text(stringResource(R.string.QrResult__share_image)) },
                onClick = { onAction(QrResultAction.ShareImage) }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.TextFields, null) },
                text = { Text(stringResource(R.string.QrResult__share_text)) },
                onClick = { onAction(QrResultAction.ShareText) }
            )
        }

        IconButton(
            onClick = { expanded = !expanded }
        ) {
            Icon(Icons.Default.Share, null)
        }
    }

    navigationIconAction { activity.finish() }

    content {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    bitmap = state.qrCodeEntity.bitmap.decodeToImageBitmap(),
                    contentDescription = "mainImage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10))
                        .aspectRatio(1f)
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                QRCodeTypeIcon(
                    modifier = Modifier.align(Alignment.TopEnd),
                    type = type
                )
                SelectionContainer(
                    Modifier.align(Alignment.Center)
                ) {
                    Text(
                        when (val info = state.barcodeInfo) {
                            is Barcode.GeoPoint -> {
                                stringResource(
                                    R.string.QrResult__point_description,
                                    info.lng,
                                    info.lat
                                )
                            }

                            else -> state.qrCodeEntity.content
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10),
                    onClick = {
                        onAction(QrResultAction.CopyText)
                    }
                ) {
                    Text(stringResource(R.string.QrResult__copy_text))
                    Spacer(modifier = Modifier.size(10.dp))
                    Icon(
                        imageVector = Icons.Outlined.TextFields,
                        contentDescription = "copy text"
                    )
                }

                Button(
                    shape = RoundedCornerShape(10),
                    onClick = {
                        onAction(QrResultAction.CopyImage)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "QR код"
                    )
                }
            }

            if (!isForView) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10),
                    onClick = { onAction(QrResultAction.Save) }
                ) {
                    Text(stringResource(R.string.QrResult__save_and_close))
                }
            }

            if (state.barcodeInfo is Barcode.UrlBookmark || state.barcodeInfo is Barcode.GeoPoint) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10),
                    onClick = {
                        onAction(QrResultAction.Open)
                    }
                ) {
                    Text(
                        when (state.barcodeInfo) {
                            is Barcode.UrlBookmark -> stringResource(R.string.QrResult__open_url)
                            else -> stringResource(R.string.QrResult__open_map) // GeoPoint
                        }
                    )
                }
            }
        }
    }
}