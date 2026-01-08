package ru.gfastg98.qr_scanner_compose.presentation.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import androidx.core.graphics.scale
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.domain.utils.throttleFist
import ru.gfastg98.qr_scanner_compose.presentation.BaseViewModel
import ru.gfastg98.qr_scanner_compose.presentation.Defaults
import ru.gfastg98.qr_scanner_compose.presentation.qr_result.QrResultActivity
import java.io.File

class QrGeneratorViewModel(
    val application: Context,
) : BaseViewModel<QrGeneratorState, QrGeneratorEvent, QrGeneratorAction>(QrGeneratorState()) {
    private val TAG = QrGeneratorViewModel::class.java.simpleName

    private val _promptFlow = MutableSharedFlow<String>()
    override fun onAction(action: QrGeneratorAction) {
        when (action) {
            is QrGeneratorAction.NewPrompt -> handleGenerate(action.string)
            QrGeneratorAction.Continue -> handleContinue()
        }
    }

    init {
        viewModelScope.launch {
            _promptFlow.throttleFist<String>(200L).collect { prompt ->
                currentState.update { it.copy(bitmap = process(prompt)) }
            }
        }
    }

    fun handleGenerate(prompt: String) {
        currentState.update { it.copy(content = prompt) }
        viewModelScope.launch { _promptFlow.emit(prompt) }
    }

    private fun process(data: String): Bitmap? =
        QRGEncoder(data, null, QRGContents.Type.TEXT, 2).let {
            it.colorBlack = Color.WHITE
            it.colorWhite = Color.BLACK
            it.bitmap?.scale(300, 300, false)
        }

    private fun handleContinue() {
        val bitmap = state.value.bitmap ?: return
        val file = File(Defaults.getBitmapIntentPath(application))
        val parent = requireNotNull(file.parentFile)
        val result = QRGSaver().save(
            parent.path,
            file.nameWithoutExtension,
            bitmap,
            QRGContents.ImageType.IMAGE_PNG
        )
        Log.i(TAG, if (result) "saved" else "no save")

        QrResultActivity.IntentBuilder(application)
            .setFileName(Defaults.QRCODE_FILE_NAME)
            .setContent(state.value.content)
            .setGenerated(true)
            .setCodeFormat(Barcode.TYPE_TEXT)
            .launch()
    }
}