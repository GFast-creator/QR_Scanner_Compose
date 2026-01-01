package ru.gfastg98.qr_scanner_compose.presentation.qr_picker

import android.app.Activity
import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntRect
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.R
import ru.gfastg98.qr_scanner_compose.presentation.Defaults
import ru.gfastg98.qr_scanner_compose.presentation.components.DETECTOR
import ru.gfastg98.qr_scanner_compose.presentation.utils.showBitmapOnActivity
import ru.gfastg98.qr_scanner_compose.presentation.utils.showToast

class QrPickerViewModel(
    val application: Application,
) : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(QrPickerScreenState())
    val state = _state.asStateFlow()

    private val _event = Channel<QrPickerScreenEvent>()
    val event = _event.receiveAsFlow()

    fun initializeWith(activity: Activity) {
        val args = requireNotNull(activity.intent.extras)
        val path = args.getString("bitmap") ?: Defaults.getBitmapIntentPath(application)

        val bitmap = path.let {
            BitmapFactory.decodeFile(
                it,
                BitmapFactory.Options().apply { inMutable = true }
            )
        }

        if (bitmap == null) {
            onEvent(QrPickerScreenEvent.Finish)
            application.showToast(R.string.QrPicker__no_qr_codes_on_photo)
            return
        }

        _state.update { it.copy(bitmap = bitmap) }


        DETECTOR.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { detections ->
                if (detections.isNotEmpty()) {
                    _state.update { state ->
                        state.copy(barcodes = detections.sortedBy { it.boundingBox!!.top })
                    }
                } else {
                    application.showToast(R.string.QrPicker__no_qr_codes_on_photo)
                    onEvent(QrPickerScreenEvent.Finish)
                }
            }.addOnFailureListener {
                application.showToast(R.string.QrPicker__error_while_processing)
                onEvent(QrPickerScreenEvent.Finish)
            }
    }

    fun onAction(action: QrPickerScreenAction) {
        when (action) {
            QrPickerScreenAction.Close -> onEvent(QrPickerScreenEvent.Finish)
            QrPickerScreenAction.Next -> _state.update { it.copy(selected = it.selected + 1) }
            QrPickerScreenAction.Previous -> _state.update { it.copy(selected = it.selected - 1) }
            QrPickerScreenAction.Save -> saveSelected()
        }
    }

    private fun saveSelected() {
        val state = _state.value
        val bitmap = requireNotNull(_state.value.bitmap)
        val barcode = requireNotNull(state.barcodes)[state.selected]
        val rect = barcode.boundingBox!!.let {
            IntRect(
                it.left,
                it.top,
                it.right,
                it.bottom
            )
        }
        showBitmapOnActivity(
            application,
            bitmap,
            rect,
            barcode,
            false
        )
    }

    private fun onEvent(event: QrPickerScreenEvent) {
        scope.launch { _event.send(event) }
    }
}