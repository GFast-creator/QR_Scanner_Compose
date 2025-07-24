package ru.gfastg98.qr_scanner_compose.presentation.screens

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.domain.QRCodeScannerViewModel
import ru.gfastg98.qr_scanner_compose.domain.utils.showBitmapOnActivity
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeScanner
import ru.gfastg98.qr_scanner_compose.presentation.components.TakePictureButton
import ru.gfastg98.qr_scanner_compose.presentation.components.rememberQRCodeScannerState
import ru.gfastg98.qr_scanner_compose.presentation.components.takePictureButtonState
import java.util.concurrent.Executor

private const val TAG = "QRCodeScannerScreen"

@Composable
fun QRCodeScannerScreen() {
    val vm = koinViewModel<QRCodeScannerViewModel>()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }

    LaunchedEffect(Unit) {
        lifecycle.addObserver(scanner)
    }

    val req = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        vm.processMedia(context, uri)
    }

    var qrCodeCameraState = rememberQRCodeScannerState()
    val state = takePictureButtonState(
        onClick = {
            qrCodeCameraState.isTakePictureRequired = it.isTouched
        }
    )
    var barcodeDetections by remember { mutableStateOf(emptyList<Barcode>()) }
    var cameraTorchState by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), Alignment.BottomCenter) {
        QRCodeScanner(
            state = qrCodeCameraState,
            onScannedAction = {
                barcodeDetections = it
            },
            onPictureTaken = { bitmap, intRect, barcode ->
                state.isTouched = false
                showBitmapOnActivity(context, bitmap, intRect, barcode)
            }
        )

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
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.4f
                                )
                            )
                            .padding(5.dp),

                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    cameraTorchState = !cameraTorchState
                    qrCodeCameraState.torchState = cameraTorchState
                },
                modifier = Modifier
                    .padding(
                        bottom = 50.dp,
                        start = 25.dp
                    )
                    .align(Alignment.BottomStart)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = if (cameraTorchState) Icons.Rounded.FlashOff
                    else Icons.Rounded.FlashOn,
                    contentDescription = "Flash light",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                )
            }

            IconButton(
                onClick = {
                    req.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        )
                    )
                }, Modifier
                    .padding(
                        bottom = 50.dp,
                        end = 25.dp
                    )
                    .align(Alignment.BottomEnd)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = "Open picture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                )
            }


            TakePictureButton(
                modifier = Modifier
                    .padding(bottom = 50.dp)
                    .size(70.dp),
                state = state
            )
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