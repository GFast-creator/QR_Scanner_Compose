package ru.gfastg98.qr_scanner_compose.presentation.components

import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.ui.geometry.Rect
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeImageAnalyzer.ScanResult
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val TAG = "QRCodeImageAnalyzer"

val PREVIEW by lazy {
    Preview.Builder()
        .setTargetResolution(Size(1080, 1920))
        /*.setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        android.util.Size(1080, 1920),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                    )
                )
                .build()
        )*/
        .build()
}


val IMAGE_ANALYSIS by lazy {
    ImageAnalysis.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAspectRatioStrategy(
                    AspectRatioStrategy(
                        AspectRatio.RATIO_16_9,
                        AspectRatioStrategy.FALLBACK_RULE_AUTO
                    )
                ).setResolutionStrategy(
                    ResolutionStrategy(
                        Size(720, 1280),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                ).build()
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
}

/*val DETECTOR: QrCodePreciseDetector<GrayU8> by lazy {
    FactoryFiducial.qrcode(null, GrayU8::class.java)!!
}*/

val DETECTOR by lazy {
    BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )
}

val SCOPE by lazy {
    CoroutineScope(Executors.newFixedThreadPool(3).asCoroutineDispatcher())
}

val EXECUTOR by lazy {
    Executor { command -> SCOPE.launch { command?.run() } }
}

@OptIn(ExperimentalGetImage::class)
fun ImageAnalysis.setBarcodeAnalyzer(
    onScanned: (List<Barcode>) -> Unit,
): ImageAnalysis {
    setAnalyzer(EXECUTOR) { imageProxy ->
        val image = imageProxy.image ?: return@setAnalyzer imageProxy.close()

        val rotation = imageProxy.imageInfo.rotationDegrees

        DETECTOR.process(
            InputImage.fromMediaImage(image, rotation)
        ).addOnSuccessListener { barcodes ->
            onScanned(barcodes)
            if (barcodes.isNotEmpty()) {
                Log.i(TAG, "New scan: ${barcodes.size} detections")
            }
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    return this
}

@OptIn(ExperimentalGetImage::class)
fun ImageAnalysis.setBarcodeAnalyzerV2(
    onDetected: (barcodes: ScanResult) -> Unit,
): ImageAnalysis {
    setAnalyzer(EXECUTOR, QRCodeImageAnalyzer(onDetected))
    return this
}

class QRCodeImageAnalyzer(
    private val onDetected: (barcodes: ScanResult) -> Unit,
) : ImageAnalysis.Analyzer {
    data class ScanResult(
        val barcodes: List<Barcode>,
        val normalizedBounds: List<Rect>,
    ) {
        fun mapped() = barcodes.zip(normalizedBounds).toMap()
    }

    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: return imageProxy.close()

        val width = imageProxy.width.toFloat()
        val height = imageProxy.height.toFloat()

        // MLKit возвращает координаты в системе ImageProxy
        DETECTOR.process(
            InputImage.fromMediaImage(
                image,
                imageProxy.imageInfo.rotationDegrees
            )
        ).addOnSuccessListener { list ->
            val barcodes = list.filterNotNull()
            val rects = barcodes.mapNotNull { barcode ->
                barcode.boundingBox?.let { box ->
                    Rect(
                        left = box.left / width,
                        top = box.top / height,
                        right = box.right / width,
                        bottom = box.bottom / height
                    )
                }
            }

            onDetected(ScanResult(barcodes, rects))
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

}

