package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.camera.core.Camera
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class QRCodeScannerState {
    var camera: Camera? by mutableStateOf(null)
    var torchState: Boolean by mutableStateOf(false)
    var isTakePictureRequired: Boolean by mutableStateOf(false)

    fun takePicture() {
        isTakePictureRequired = true
    }
}