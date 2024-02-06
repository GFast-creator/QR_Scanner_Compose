package ru.gfastg98.qr_scanner_compose.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGSaver
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
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
import androidx.compose.material.icons.sharp.FlashOff
import androidx.compose.material.icons.sharp.FlashOn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.ContactInfo
import com.google.mlkit.vision.common.InputImage
import ru.gfastg98.qr_scanner_compose.QRPickerActivity
import ru.gfastg98.qr_scanner_compose.QRResultActivity
import java.io.Serializable
import java.util.concurrent.Executor


class QRScannerActivity : ComponentActivity() {


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
        } else {
            Log.i("kilo", "Permission denied")
        }
    }


    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermission()
        setContent {
            QRCodeScannerPreview(
            )
        }
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

fun saveBitmap(bitmap: Bitmap, filename: String) {

}

fun showBitmapOnActivity(bitmap: Bitmap, barcode: Barcode, applicationContext: Context) {
    val b = with(barcode.boundingBox!!) {
        val r1 = (left - 60).coerceAtLeast(0)
        val r2 = (top - 60).coerceAtLeast(0)
        Bitmap.createBitmap(
            bitmap,
            r1,
            r2,
            (right - left + 120).coerceAtMost(bitmap.width - r1 - 1),
            (bottom - top + 120).coerceAtMost(bitmap.height - r2 - 1)
        )
    }


    val filename = "intent"
    Log.i(
        "kilo", if (QRGSaver().save(
                applicationContext.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES
                )!!.path + "/QRCODES/", filename, b, QRGContents.ImageType.IMAGE_PNG
            )
        ) "saved" else "no save"
    )

    applicationContext.startActivity(
        Intent(
            applicationContext,
            QRResultActivity::class.java
        )
            .putExtra("file_name", "$filename.png")
            .putExtra("content", barcode.displayValue)
            .putExtra("generated", false)
            .apply {
                when (barcode.valueType) {
                    Barcode.TYPE_CONTACT_INFO ->  Gson().toJson(barcode.contactInfo)
                    Barcode.TYPE_WIFI -> Gson().toJson(barcode.wifi)
                    Barcode.TYPE_PHONE -> Gson().toJson(barcode.phone)
                    Barcode.TYPE_URL -> Gson().toJson(barcode.url)
                    Barcode.TYPE_EMAIL ->  Gson().toJson(barcode.email)
                    Barcode.TYPE_GEO -> Gson().toJson(barcode.geoPoint)
                    Barcode.TYPE_CALENDAR_EVENT -> Gson().toJson(barcode.calendarEvent)
                    else -> null
                }?.let { obj ->
                    putExtra("barcode_obj", obj)
                    Log.e("kilo", obj)
                }

                putExtra("code_format", barcode.valueType)

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    )
}

private fun Context.mainExecutor(): Executor {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        mainExecutor
    } else {
        HandlerExecutor(mainLooper)
    }
}

@Composable
fun QRCodeScannerPreview(
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    var cameraResolutionCoeff by remember {
        mutableStateOf<Pair<Float, Float>?>(null)
    }
    var takePictureFlag by remember {
        mutableStateOf(false)
    }
    val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )
    lifecycle.addObserver(scanner)

    val req = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        it?.let {
            val pickedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        context.contentResolver,
                        it
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }

            if (
                QRGSaver().save(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path + "/QRCODES/",
                    "intent",
                    pickedBitmap,
                    QRGContents.ImageType.IMAGE_PNG
                )
            )
                context.startActivity(
                    Intent(
                        context,
                        QRPickerActivity::class.java
                    )
                        .putExtra("bitmap", "intent")
                )

            pickedBitmap.recycle()
        }
    }

    var count = 0

    var barcodeDetections by remember {
        mutableStateOf(emptyList<Barcode>())
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
            )
            setImageAnalysisAnalyzer(
                context.mainExecutor(),
                object : ImageAnalysis.Analyzer {

                    var ready = true

                    @OptIn(ExperimentalGetImage::class)
                    override fun analyze(imageProxy: ImageProxy) {

                        if (count == Int.MAX_VALUE - 1) count = 0
                        if (ready) {

                            ready = false

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

                                        if (takePictureFlag && it.isNotEmpty()) {
                                            val m = android.graphics.Matrix()
                                            m.postRotate(90f)
                                            val b = imageProxy.toBitmap()
                                            val bitmap = Bitmap.createBitmap(
                                                b,
                                                0, 0, b.width, b.height, m, false
                                            )
                                            showBitmapOnActivity(
                                                bitmap,
                                                it.first(),
                                                context
                                            )

                                            takePictureFlag = false
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
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
                .drawWithContent {
                    drawContent()
                    val path = Path()
                    if (barcodeDetections.isNotEmpty()) {
                        barcodeDetections.forEachIndexed { index, item ->
                            item.cornerPoints?.let { arrayOfPoints ->
                                cameraResolutionCoeff?.let { pair ->
                                    path.moveTo(
                                        arrayOfPoints[0].x.toFloat() * size.width / pair.first,
                                        arrayOfPoints[0].y.toFloat() * size.height / pair.second
                                    )
                                    path.lineTo(
                                        arrayOfPoints[1].x.toFloat() * size.width / pair.first,
                                        arrayOfPoints[1].y.toFloat() * size.height / pair.second
                                    )
                                    path.lineTo(
                                        arrayOfPoints[2].x.toFloat() * size.width / pair.first,
                                        arrayOfPoints[2].y.toFloat() * size.height / pair.second
                                    )
                                    path.lineTo(
                                        arrayOfPoints[3].x.toFloat() * size.width / pair.first,
                                        arrayOfPoints[3].y.toFloat() * size.height / pair.second
                                    )
                                    path.close()

                                    drawPath(
                                        path,
                                        if (index == 0) Color.Red
                                        else Color.Blue,
                                        style = Stroke(width = 20f)
                                    )
                                }
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
            Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            var cameraTorchState by remember {
                mutableStateOf(false)
            }
            IconButton(
                onClick = {
                    if (controller.cameraInfo!!.hasFlashUnit()) {
                        cameraTorchState = !cameraTorchState
                        controller.enableTorch(cameraTorchState)
                    }
                }, Modifier
                    .padding(
                        bottom = 50.dp,
                        start = 25.dp
                    )
                    //.border(1.dp, Color.Blue)
                    .align(Alignment.BottomStart)
                    .size(50.dp)
            ) {

                Icon(
                    imageVector = if (cameraTorchState)
                        Icons.Sharp.FlashOff else Icons.Sharp.FlashOn,
                    contentDescription = "Open picture",
                    tint = Color.Gray,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                )
            }

            IconButton(
                onClick = {
                    req.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }, Modifier
                    .padding(
                        bottom = 50.dp,
                        end = 25.dp
                    )
                    //.border(1.dp, Color.Blue)
                    .align(Alignment.BottomEnd)
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
                    takePictureFlag = true
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


@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
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
                controller.imageAnalysisTargetSize = CameraController.OutputSize(Size(720, 1280))
                //controller.imageAnalysisTargetSize = CameraController.OutputSize(Size(1080, 1920))
            }
        }
    )
}