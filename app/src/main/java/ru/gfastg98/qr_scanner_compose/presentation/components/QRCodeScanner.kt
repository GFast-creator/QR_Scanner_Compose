package ru.gfastg98.qr_scanner_compose.presentation.components

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import ru.gfastg98.qr_scanner_compose.domain.utils.mapUsingPreviewTransform
import ru.gfastg98.qr_scanner_compose.presentation.utils.drawBarcodeSelection
import ru.gfastg98.qr_scanner_compose.presentation.utils.toAspectRatio


private const val TAG = "QRCodeScanner"

@Composable
fun rememberQRCodeScannerState() = remember { QRCodeScannerState() }


@Composable
fun QRCodeScanner(
    modifier: Modifier = Modifier,
    state: QRCodeScannerState = rememberQRCodeScannerState(),
    onNewScan: (List<Barcode>) -> Unit,
    onPictureTaken: (Bitmap, IntRect, Barcode) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var init by remember { mutableStateOf(false) }

    var center by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(Size.Zero) }

    var previewResolution = remember { Size(1080f, 1920f) }
    var analyzerResolution = remember { Size(720f, 1280f) }

    var barcodes by remember { mutableStateOf<List<Barcode>?>(null) }

    var previewComponent by remember { mutableStateOf<PreviewView?>(null) }
    val animatedBoundsTarget = remember(barcodes, size, previewComponent) {
        barcodes
            ?.firstOrNull()
            ?.boundingBox
            ?.toComposeRect()
            ?.let {
                previewComponent?.let { previewView ->
                    it.mapUsingPreviewTransform(previewView = previewView)
                }
            }/*?.translate(analyzerResolution.normalize(), size.normalize())*/ ?: Rect(center, 300f)
    }

    val animatedBounds by animateRectAsState(animatedBoundsTarget)
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary

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

    Box(modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .align(Alignment.TopStart)
                .aspectRatio(previewResolution.toAspectRatio())
                .onPlaced {
                    center = it.size.center.toOffset()
                    init = true
                }
                .alpha(0.99f) // Для корректной анимации
                .drawWithContent {
                    if (init) {
                        drawContent()
                        drawBarcodeSelection(
                            animatedBounds,
                            barcodes?.isNotEmpty() == true,
                            primaryColor
                        )
                    }
                }
                .fillMaxSize(),
            factory = { context ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FIT_START
                }

                val imageAnalyzer = IMAGE_ANALYSIS.setBarcodeAnalyzerV2 { result ->
                    barcodes = result.barcodes.also(onNewScan)
                }

                cameraProviderFuture.addListener(
                    {
                        // Used to bind the lifecycle of cameras to the lifecycle owner
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        // Preview
                        val preview = PREVIEW.also {
                            it.surfaceProvider = previewComponent!!.surfaceProvider
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
                            ).also { camera ->
                                cameraControl = camera.cameraControl

                                preview.resolutionInfo?.resolution?.let {
                                    previewResolution =
                                        Size(it.width.toFloat(), it.height.toFloat())
                                    Log.i(
                                        TAG,
                                        "QRCodeScanner: new preview resolution: $analyzerResolution"
                                    )
                                }

                                imageAnalyzer.resolutionInfo?.resolution?.let {
                                    analyzerResolution =
                                        Size(it.width.toFloat(), it.height.toFloat())
                                    Log.i(TAG, "QRCodeScanner: new resolution: $analyzerResolution")
                                }
                            }
                        } catch (exc: Exception) {
                            Log.e(TAG, "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(context)
                )
                previewComponent!!
            },
        )
    }
}
