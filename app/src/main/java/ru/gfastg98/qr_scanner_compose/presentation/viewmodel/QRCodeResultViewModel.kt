package ru.gfastg98.qr_scanner_compose.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.BuildConfig
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.domain.utils.processQRCodeInfo
import ru.gfastg98.qr_scanner_compose.presentation.activity.QRResultActivity
import ru.gfastg98.qr_scanner_compose.presentation.state.QRCodeResultState
import ru.gfastg98.qr_scanner_compose.presentation.utils.showToast
import java.io.File

class QRCodeResultViewModel(
    context: Context,
    intent: Intent,
    private val db: AppDatabase,
) : ViewModel() {
    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val _state = MutableStateFlow(
        processIntent(context, intent)
    )
    val state = _state.asStateFlow()

    fun processIntent(context: Context, intent: Intent): QRCodeResultState {
        val f = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val filename = intent.getStringExtra("file_name") ?: "intent.png"
        val file = File("$f/QRCODES/$filename")

        val qrCodeEntity = QRCodeEntity(
            uid = 0,
            bitmap = file.readBytes(),
            content = intent.getStringExtra("content") ?: "",
            generated = intent.getBooleanExtra("generated", false),
            barcodeObjectJson = intent.getStringExtra("barcode_obj") ?: "",
            codeFormat = intent.getIntExtra(QRResultActivity.EXTRA_CODE_FORMAT, -1)
        )

        val barcodeInfo = qrCodeEntity.processQRCodeInfo()

        return QRCodeResultState(
            qrCodeEntity,
            barcodeInfo,
            file
        )
    }

    fun shareWithText(context: Context) {
        val uri = FileProvider.getUriForFile(
            context,
            "ru.gfastg98.qr_scanner_compose.provider",
            _state.value.file
        )

        Intent.createChooser(
            Intent(Intent.ACTION_SEND)
                .apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, _state.value.qrCodeEntity.content)
                    type = "text/plain"
                },
            "Поделиться текстом"
        ).also {
            if (it.resolveActivity(context.packageManager) != null)
                context.startActivity(it)
            else context.showToast("Нет приложения для отправки")
        }
    }

    fun shareWithPhoto(context: Context) {
        val uri = FileProvider.getUriForFile(
            context,
            "ru.gfastg98.qr_scanner_compose.provider",
            _state.value.file
        )

        ShareCompat.IntentBuilder(context)
            .setStream(uri)
            .setText(_state.value.qrCodeEntity.content)
            .setChooserTitle("Отправить QR-код")
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .let {
                if (it.resolveActivity(context.packageManager) != null)
                    context.startActivity(it)
                else context.showToast("Нет приложения для отправки")
            }
    }

    fun saveToDatabase() {
        viewModelScope.launch { db.qrCodeDao().insertAll(_state.value.qrCodeEntity) }
    }

    fun copyToClipboard(context: Context) {
        val clipData = ClipData.newPlainText(
            "Текст QR-кода",
            _state.value.qrCodeEntity.content
        )
        clipboardManager.setPrimaryClip(clipData)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            context.showToast("Текст скопирован")
    }

    fun copyToClipboardImage(context: Context) {
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            _state.value.file
        )

        clipboardManager.setPrimaryClip(
            ClipData.newUri(
                context.contentResolver,
                "Скопированный QR код",
                uri
            )
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            context.showToast("Картинка скопирована")
    }
}

