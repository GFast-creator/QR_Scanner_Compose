package ru.gfastg98.qr_scanner_compose.presentation.scanner

import com.google.mlkit.vision.barcode.common.Barcode

data class QrScannerState(
    val status: Status = Status.INITIALIZING,
    val barcodeDetections: List<Barcode> = emptyList(),
    var isScanning: Boolean = false,
) {
    enum class Status {
        INITIALIZING,
        READY
    }
}