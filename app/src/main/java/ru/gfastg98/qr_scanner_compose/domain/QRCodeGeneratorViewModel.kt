package ru.gfastg98.qr_scanner_compose.domain

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.QRResultActivity
import ru.gfastg98.qr_scanner_compose.QRResultActivity.Companion.EXTRA_CODE_FORMAT
import ru.gfastg98.qr_scanner_compose.domain.utils.throttleFist


class QRCodeGeneratorViewModel : ViewModel() {
    companion object {
        val TAG = QRCodeGeneratorViewModel::class.java.simpleName
    }

    private val _generationResult = MutableStateFlow<Bitmap?>(null)
    val generationResult = _generationResult.asStateFlow()

    private val _promptFlow = MutableSharedFlow<String>()

    init {
        viewModelScope.launch {
            _promptFlow.throttleFist<String>(200L)
                .collect {
                    _generationResult.value = process(it)
                }
        }
    }

    fun generate(prompt: String) {
        viewModelScope.launch { _promptFlow.emit(prompt) }
    }

    private fun process(data: String): Bitmap? =
        QRGEncoder(data, null, QRGContents.Type.TEXT, 2).let {
            it.colorBlack = Color.WHITE
            it.colorWhite = Color.BLACK

            it.bitmap?.scale(300, 300, false)
        }

    fun viewFull(
        context: Context,
        bitmap: Bitmap,
        content: String,
    ) {
        val filename = "intent"
        val result = QRGSaver().save(
            context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            )!!.path + "/QRCODES/",
            filename,
            bitmap,
            QRGContents.ImageType.IMAGE_PNG
        )
        Log.i(TAG, if (result) "saved" else "no save")

        context.startActivity(
            Intent(
                context,
                QRResultActivity::class.java
            )
                .putExtra("file_name", "$filename.png")
                .putExtra("content", content)
                .putExtra("generated", true)
                .putExtra(EXTRA_CODE_FORMAT, Barcode.TYPE_TEXT)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}