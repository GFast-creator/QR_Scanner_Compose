package ru.gfastg98.qr_scanner_compose.presentation.scanner

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.mlkit.vision.barcode.common.Barcode
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.presentation.ObserveAsEvents
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeScanner
import ru.gfastg98.qr_scanner_compose.presentation.components.TakePictureButton
import ru.gfastg98.qr_scanner_compose.presentation.components.rememberQRCodeScannerState
import java.util.concurrent.Executor

private const val TAG = "QRCodeScannerScreen"

@Composable
fun QrCodeScannerScreen() {
    val viewModel = koinViewModel<QrScannerViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { _ ->
        /* no-op */
    }

    QrCodeScannerScreenRoot(
        state,
        viewModel::onAction
    )
}

@Composable
fun QrCodeScannerScreenRoot(
    state: QrScannerState,
    onAction: (QrScannerAction) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAction(QrScannerAction.ChooseMedia(uri))
        }
    }

    val qrCodeCameraState = rememberQRCodeScannerState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        QRCodeScanner(
            state = qrCodeCameraState,
            onNewScan = { onAction(QrScannerAction.NewSuggestion(it)) },
            onPictureTaken = { bitmap, intRect, barcode ->
                onAction(QrScannerAction.NewScan(bitmap, intRect, barcode))
            }
        )

        Detections(state.barcodeDetections)

        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(52.dp),
                    onClick = { qrCodeCameraState.torchState = !qrCodeCameraState.torchState }
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = if (qrCodeCameraState.torchState) Icons.Rounded.FlashOff
                        else Icons.Rounded.FlashOn,
                        contentDescription = "Flash light",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TakePictureButton(
                    modifier = Modifier.size(70.dp),
                    checked = state.isScanning,
                    onCheckedChanged = { newValue ->
                        onAction(QrScannerAction.SetScanMode(newValue))
                        qrCodeCameraState.isTakePictureRequired = newValue
                    }
                )
                IconButton(
                    modifier = Modifier.size(52.dp),
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            )
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = "Open picture",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.Detections(barcodeDetections: List<Barcode>) {
    Column(
        Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
    ) {
        barcodeDetections.forEach {
            it.rawValue?.let { m ->
                Text(
                    text = m,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(5.dp),

                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
            }
        }
    }
}

private fun Context.mainExecutor(): Executor {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        mainExecutor
    } else {
        HandlerExecutor(mainLooper)
    }
}