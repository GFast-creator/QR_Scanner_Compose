package ru.gfastg98.qr_scanner_compose.presentation.qr_picker

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.common.Barcode

data class QrPickerScreenState(
    val status: Status = Status.INITIALIZING,
    val bitmap: Bitmap? = null,
    val barcodes: List<Barcode>? = null,
    val selected: Int = 0,
) {
    enum class Status {
        INITIALIZING, READY,
    }

    fun isReady() = status == Status.READY
}