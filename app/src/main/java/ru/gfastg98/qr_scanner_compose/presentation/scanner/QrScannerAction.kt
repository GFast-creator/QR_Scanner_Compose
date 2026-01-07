package ru.gfastg98.qr_scanner_compose.presentation.scanner

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.IntRect
import com.google.mlkit.vision.barcode.common.Barcode

sealed interface QrScannerAction {
    data class ChooseMedia(val media: Uri) : QrScannerAction
    data class NewScan(val bitmap: Bitmap, val intRect: IntRect, val barcode: Barcode) :
        QrScannerAction

    data class NewSuggestion(val barcode: List<Barcode>) : QrScannerAction

    data object ToggleScan : QrScannerAction
    data class SetScanMode(val scanning: Boolean) : QrScannerAction
}