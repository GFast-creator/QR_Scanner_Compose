package ru.gfastg98.qr_scanner_compose.presentation.scanner

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGSaver
import androidx.compose.ui.unit.IntRect
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.gfastg98.qr_scanner_compose.presentation.BaseViewModel
import ru.gfastg98.qr_scanner_compose.presentation.Defaults
import ru.gfastg98.qr_scanner_compose.presentation.qr_picker.QrPickerActivity
import ru.gfastg98.qr_scanner_compose.presentation.utils.showBitmapOnActivity
import java.io.File

class QrScannerViewModel(
    val application: Context,
) : BaseViewModel<QrScannerState, QrScannerEvent, QrScannerAction>(QrScannerState()) {
    override fun onAction(action: QrScannerAction) {
        when (action) {
            is QrScannerAction.ChooseMedia -> processMedia(action.media)
            is QrScannerAction.NewScan -> handleNewScan(
                action.bitmap,
                action.intRect,
                action.barcode
            )


            is QrScannerAction.SetScanMode -> currentState.update {
                it.copy(isScanning = action.scanning)
            }

            QrScannerAction.ToggleScan -> currentState.update {
                it.copy(isScanning = !it.isScanning)
            }

            is QrScannerAction.NewSuggestion -> currentState.update {
                it.copy(barcodeDetections = action.barcode)
            }
        }
    }

    fun handleNewScan(bitmap: Bitmap, intRect: IntRect, barcode: Barcode) {
        showBitmapOnActivity(application, bitmap, intRect, barcode, false)
        currentState.update { it.copy(isScanning = false) }
    }

    fun processMedia(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val resolver = application.contentResolver

        val pickedBitmap = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        resolver,
                        uri
                    )
                )
            } else {
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        }.getOrElse { e ->
            e.printStackTrace()
            return@launch
        }

        val file = File(Defaults.getBitmapIntentPath(application))
        val parent = file.parentFile?.absolutePath ?: return@launch

        val result = QRGSaver().save(
            parent,
            file.nameWithoutExtension,
            pickedBitmap,
            QRGContents.ImageType.IMAGE_PNG
        )

        if (result) {
            withContext(Dispatchers.Main) {
                application.startActivity(
                    Intent(application, QrPickerActivity::class.java)
                        .putExtra("bitmap", "intent")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}