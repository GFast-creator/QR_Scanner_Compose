package ru.gfastg98.qr_scanner_compose.presentation.components

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import ru.gfastg98.qr_scanner_compose.domain.utils.translate
import ru.gfastg98.qr_scanner_compose.presentation.utils.drawBarcodeSelection


private const val TAG = "QRCodeScanner"

@Composable
fun rememberQRCodeScannerState() = remember { QRCodeScannerState() }


@Composable
fun QRCodeScanner(
    modifier: Modifier = Modifier,
    state: QRCodeScannerState = rememberQRCodeScannerState(),
    onScannedAction: (List<Barcode>) -> Unit,
    onPictureTaken: (Bitmap, IntRect, Barcode) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var init by remember { mutableStateOf(false) }

    var center by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(Size.Zero) }

    var previewResolution = remember { Size(1080f, 1920f) }
    var analyzerResolution = remember { Size(720f, 1280f) }

    var barcodes by remember { mutableStateOf<List<Barcode>?>(null) }

    val animatedBoundsTarget = remember(barcodes, size) {
        barcodes
            ?.firstOrNull()
            ?.boundingBox
            ?.toComposeRect()
            ?.translate(analyzerResolution, size) ?: Rect(center, 300f)
    }

    val animatedBounds by animateRectAsState(animatedBoundsTarget)
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var previewComponent by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(state.torchState) {
        cameraControl?.enableTorch(state.torchState)
    }

    LaunchedEffect(state.isTakePictureRequired, barcodes) {
        if (!state.isTakePictureRequired) return@LaunchedEffect
        val bitmap = previewComponent?.bitmap ?: return@LaunchedEffect
        val result = barcodes?.firstOrNull() ?: return@LaunchedEffect

        state.isTakePictureRequired = false

        onPictureTaken(bitmap, animatedBoundsTarget.roundToIntRect(), result)
    }

    Box(
        Modifier
            .aspectRatio(1080f / 1920f)
            .fillMaxWidth()
    ) {
        AndroidView(
            modifier = modifier
                .align(Alignment.TopStart)
                .onPlaced {
                    center = it.size.center.toOffset()
                    size = it.size.toSize()

                    init = true
                }
                .alpha(0.99f) // Для корректной анимации
                .drawWithContent {
                    if (init) {
                        drawContent()
                        drawBarcodeSelection(animatedBounds, barcodes?.firstOrNull() != null)
                    }
                }
                .fillMaxSize(),
            factory = { context ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                previewComponent = PreviewView(context).apply {
                    //scaleType = PreviewView.ScaleType.FILL_START
                    scaleType = PreviewView.ScaleType.FIT_START
                }

                val imageAnalyzer = IMAGE_ANALYSIS.setAnalyzer(
                    onScanned = { list ->
                        barcodes = list.also(onScannedAction)
                    }
                )

                imageAnalyzer.resolutionInfo?.resolution?.let {
                    analyzerResolution = Size(it.width.toFloat(), it.height.toFloat())
                }

                cameraProviderFuture.addListener(
                    {
                        // Used to bind the lifecycle of cameras to the lifecycle owner
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        // Preview
                        val preview = PREVIEW.also {
                            it.surfaceProvider = previewComponent!!.surfaceProvider
                        }

                        preview.resolutionInfo?.resolution?.let {
                            previewResolution = Size(it.width.toFloat(), it.height.toFloat())
                        }

                        // Select back camera as a default
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            // Unbind use cases before rebinding
                            cameraProvider.unbindAll()
                            // Bind use cases to camera
                            state.camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer,
                            ).also {
                                cameraControl = it.cameraControl
                            }
                        } catch (exc: Exception) {
                            Log.e(TAG, "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(context)
                )
                previewComponent!!
            },
        )

        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.2f))
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            barcodes?.forEach {
                Text(it.rawValue ?: "")
            }
        }
    }
}
