package ru.gfastg98.qr_scanner_compose.domain

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
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
import ru.gfastg98.qr_scanner_compose.domain.utils.showToast
import java.io.File

class QRCodeResultViewModel(
    context: Context,
    intent: Intent,
    private val db: AppDatabase
) : ViewModel() {
    private val _state = MutableStateFlow<QRCodeResultState>(processIntent(context, intent))
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
            codeFormat = intent.getIntExtra("code_format", -1)
        )

        val barcodeInfo = qrCodeEntity.processQRCodeInfo()

        return QRCodeResultState(
            qrCodeEntity,
            barcodeInfo,
            file
        )
    }

    fun shareAction(context: Context, qrCodeEntity: QRCodeEntity) {
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val uri = FileProvider.getUriForFile(
            context,
            "ru.gfastg98.qr_scanner_compose.provider",
            _state.value.file!!
        )

        Intent.createChooser(
            Intent(Intent.ACTION_SEND)
                .apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, qrCodeEntity.content)
                    type = "*/*"
                },
            "Отправить QR-код"
        ).also {
            if (it.resolveActivity(context.packageManager) != null)
                context.startActivity(it)
            else Toast.makeText(
                context,
                "Нет приложения для отправки",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun saveToDatabase() {
        viewModelScope.launch { db.qrCodeDao().insertAll(_state.value.qrCodeEntity!!) }
    }

    fun copyToClipboard(context: Context) {
        _state.value.qrCodeEntity ?: return
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.setPrimaryClip(
            ClipData.newPlainText(
                "Текст QR-кода",
                _state.value.qrCodeEntity!!.content
            )
        )
        context.showToast("Скопировано")
    }

    fun copyToClipboardImage(context: Context) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            _state.value.file!!
        )

        clipboardManager.setPrimaryClip(
            ClipData.newUri(
                context.contentResolver,
                "QR code",
                uri
            )
        )
        context.showToast("Картинка скопирована")
    }
}