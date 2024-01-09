package ru.gfastg98.qr_scanner_compose

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FolderOpen
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.io.FileOutputStream
import java.lang.Boolean
import java.util.concurrent.Executor
import kotlin.math.abs


class MainActivity : ComponentActivity() {

    private var cameraResolutionCoeff: Pair<Float, Float>? = null
    private var rotationDegrees: Int? = null
    private var takePicture = false
    lateinit var dbHelper: DBHelper

    private fun Context.mainExecutor(): Executor {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mainExecutor
        } else {
            HandlerExecutor(mainLooper)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
        } else {
            Log.i("kilo", "Permission denied")
        }
    }

    private lateinit var controller: LifecycleCameraController

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DBHelper(applicationContext)

        setContent {
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
            )
            lifecycle.addObserver(scanner)

            var count = 0

            var barcodeDetections by remember {
                mutableStateOf(emptyList<Barcode>())
            }

            controller = remember {
                LifecycleCameraController(applicationContext).apply {
                    setEnabledUseCases(
                        CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
                    )
                    setImageAnalysisAnalyzer(
                        applicationContext.mainExecutor(),
                        object : ImageAnalysis.Analyzer {

                            var ready = true

                            override fun analyze(imageProxy: ImageProxy) {

                                if (count == Int.MAX_VALUE - 1) count = 0



                                if (ready) {
                                    ready = false
                                    rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                    /* val m = android.graphics.Matrix()
                                     m.postRotate(90f)
                                     val bitmap = Bitmap.createBitmap(
                                         imageProxy.toBitmap(),
                                         0,
                                         0,
                                         imageProxy.width,
                                         imageProxy.height,
                                         m,
                                         false
                                     )*/

                                    val mediaImage: Image? = imageProxy.image
                                    val fin = {
                                        imageProxy.close()
                                        mediaImage?.close()
                                        ready = true
                                    }
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            90
                                            //imageProxy.imageInfo.rotationDegrees
                                        )

                                        if (cameraResolutionCoeff == null)
                                            cameraResolutionCoeff =
                                                imageProxy.height.toFloat() to imageProxy.width.toFloat()
                                        scanner.process(image)
                                            .addOnSuccessListener {
                                                barcodeDetections = it
                                                //Toast.makeText(applicationContext,x,Toast.LENGTH_SHORT).show()
                                                //Log.e("kilo", it.size.toString())

                                                if (takePicture && it.isNotEmpty()) {
                                                    val m = android.graphics.Matrix()
                                                    m.postRotate(90f)

                                                    val bitmap = with(it.first().boundingBox!!) {
                                                        Bitmap.createBitmap(
                                                            Bitmap.createBitmap(
                                                                imageProxy.toBitmap(),
                                                                0,
                                                                0,
                                                                imageProxy.width,
                                                                imageProxy.height,
                                                                m,
                                                                false
                                                            ),
                                                            left,
                                                            top,
                                                            right - left,
                                                            bottom - top
                                                        )
                                                    }


                                                    val filename = "intent.png"
                                                    with(
                                                        File(
                                                            applicationContext.getExternalFilesDir(
                                                                Environment.DIRECTORY_PICTURES
                                                            ).toString() + "/QRCODES"
                                                        )
                                                    ) {
                                                        mkdir()
                                                        val f = File(path, filename)
                                                        f.createNewFile()

                                                        if (f.exists() and f.isFile) {

                                                            Log.e(
                                                                "kilo",
                                                                f.absolutePath
                                                            )
                                                            Log.e(
                                                                "kilo",
                                                                f.isFile.toString()
                                                            )
                                                            Log.e(
                                                                "kilo",
                                                                f.isDirectory.toString()
                                                            )
                                                            Log.e(
                                                                "kilo",
                                                                f.canWrite().toString()
                                                            )
                                                            Log.e("kilo", "1")

                                                            with(FileOutputStream(f)) {
                                                                bitmap.compress(
                                                                    Bitmap.CompressFormat.PNG,
                                                                    100,
                                                                    this
                                                                )
                                                                flush()
                                                                close()
                                                            }

                                                            startActivity(
                                                                Intent(
                                                                    applicationContext,
                                                                    QRResultActivity::class.java
                                                                )
                                                                    .putExtra("file_name", filename)
                                                            )
                                                        }
                                                    }

                                                    takePicture = false
                                                }

                                                fin()
                                            }
                                            .addOnFailureListener {
                                                //Toast.makeText(applicationContext,it.message,Toast.LENGTH_SHORT).show()
                                                it.message?.let { it1 -> Log.e("kilo", it1) }
                                                fin()
                                            }
                                            .addOnCompleteListener { fin() }
                                            .addOnCanceledListener { fin() }

                                    }
                                } else {
                                    imageProxy.close()
                                }
                                count++
                            }
                        }
                    )
                }
            }
            Box(Modifier.fillMaxSize(), Alignment.BottomCenter) {
                CameraPreview(
                    scaleType = PreviewView.ScaleType.FILL_CENTER,
                    controller = controller,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopStart)
                        .drawWithContent {
                            drawContent()
                            val path = Path()
                            if (barcodeDetections.isNotEmpty()) {
                                barcodeDetections.first().cornerPoints?.let {
                                    cameraResolutionCoeff?.let { pair ->
                                        val coeffX = abs(size.width - pair.first) / 2
//                                        Log.i("kilo", coeffX.toString())
                                        path.moveTo(
                                            it[0].x.toFloat() * size.width / pair.first,
                                            it[0].y.toFloat() * size.height / pair.second
                                        )
                                        path.lineTo(
                                            it[1].x.toFloat() * size.width / pair.first,
                                            it[1].y.toFloat() * size.height / pair.second
                                        )
                                        path.lineTo(
                                            it[2].x.toFloat() * size.width / pair.first,
                                            it[2].y.toFloat() * size.height / pair.second
                                        )
                                        path.lineTo(
                                            it[3].x.toFloat() * size.width / pair.first,
                                            it[3].y.toFloat() * size.height / pair.second
                                        )
                                        path.close()

                                        //Log.e("kilo", it.joinToString { s -> "${s.x} ${s.y}" })

                                        val m = Matrix()
//                                        m.scale(
//                                            size.width/pair.first,
//                                            size.height/pair.second
//                                        )

                                        path.transform(m)

                                        drawPath(path, Color.Red, style = Stroke(width = 20f))
                                    }
                                }
                            }
                        }
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    barcodeDetections.forEach {
                        it.displayValue?.let { it1 ->
                            Text(
                                it1,
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
                    Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {

                        }, Modifier
                            .padding(
                                bottom = 50.dp,
                                start = 300.dp
                            )
                            //.border(1.dp, Color.Blue)
                            .size(50.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Sharp.FolderOpen,
                            contentDescription = "Open picture",
                            tint = Color.Gray,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(1.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            takePicture = true
                        }, Modifier
                            .padding(bottom = 50.dp)
                            //.border(1.dp, Color.Blue)
                            .size(70.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Sharp.Lens,
                            contentDescription = "Take picture button",
                            tint = Color.Gray,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(1.dp)
                                .border(1.dp, Color.Gray, CircleShape)
                        )
                    }
                }
            }
        }
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    scaleType: PreviewView.ScaleType,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    modifier: Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(modifier = modifier,
        factory = { context ->

            PreviewView(context).apply {
                this.controller = controller
                controller.cameraSelector = cameraSelector
                controller.unbind()
                controller.bindToLifecycle(lifecycleOwner)
                controller.previewTargetSize = CameraController.OutputSize(Size(1080, 1920))
                controller.imageAnalysisTargetSize = CameraController.OutputSize(Size(360, 640))
                //controller.imageAnalysisTargetSize = CameraController.OutputSize(Size(1080, 1920))
            }
        })


}