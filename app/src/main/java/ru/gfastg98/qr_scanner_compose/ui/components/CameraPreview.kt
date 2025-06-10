package ru.gfastg98.qr_scanner_compose.ui.components

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    modifier: Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier.alpha(0.99f),
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