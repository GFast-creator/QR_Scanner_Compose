package ru.gfastg98.qr_scanner_compose.presentation.components

import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
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

        /*.setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        android.util.Size(720, 1280),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
                    )
                )
                .build()
        )*/
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

@OptIn(ExperimentalGetImage::class)
fun ImageAnalysis.setAnalyzer(
    onScanned: (List<Barcode>) -> Unit
): ImageAnalysis {
    setAnalyzer(
        /* executor = */ { runnable -> SCOPE.launch { runnable.run() } }
    ) { imageProxy ->
        Log.i(TAG, "analyzer: run")
        val image = imageProxy.image ?: let {
            imageProxy.close()
            return@setAnalyzer
        }

        val rotation = imageProxy.imageInfo.rotationDegrees

        DETECTOR.process(
            InputImage.fromMediaImage(image, rotation)
        ).addOnSuccessListener { barcodes ->
            onScanned(barcodes)
        }.addOnCompleteListener {
            imageProxy.close()
        }

    }

    return this
}

