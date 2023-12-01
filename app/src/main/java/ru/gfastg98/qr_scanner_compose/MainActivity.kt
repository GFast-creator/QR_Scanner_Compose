package ru.gfastg98.qr_scanner_compose

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FolderOpen
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.lang.reflect.Executable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
        } else {
            Log.i("kilo", "Permission denied")
        }
    }

    private lateinit var cameraExecutor : Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            Box(Modifier.fillMaxSize(), Alignment.BottomCenter) {
                val imageCapture = remember {
                    ImageCapture.Builder().build()
                }
                CameraPreview(PreviewView.ScaleType.FILL_CENTER, imageCapture = imageCapture)
                Box(
                    Modifier,
                    contentAlignment = Alignment.Center
                ){
                    IconButton(
                        onClick = {
                            /*TODO*/
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
                            takePicture(imageCapture)
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



    private fun takePicture(imageCapture: ImageCapture) {
        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback(){
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                runOnUiThread {
                    Toast.makeText(applicationContext, "pic is taken", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
}


@Composable
fun CameraPreview(
    scaleType: PreviewView.ScaleType,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    imageCapture: ImageCapture
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView({ context ->
        val previewView = PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.scaleType = scaleType
        }

        val previewUseCase = Preview.Builder().build()
        previewUseCase.setSurfaceProvider(previewView.surfaceProvider)

        coroutineScope.launch {
            val cameraProvider = context.cameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase, imageCapture)
        }

        previewView
    })

}

suspend fun Context.cameraProvider(): ProcessCameraProvider = suspendCoroutine {
    val listanableFuture = ProcessCameraProvider.getInstance(this)
    listanableFuture.addListener({
        it.resume(listanableFuture.get())
    }, ContextCompat.getMainExecutor(this))
}