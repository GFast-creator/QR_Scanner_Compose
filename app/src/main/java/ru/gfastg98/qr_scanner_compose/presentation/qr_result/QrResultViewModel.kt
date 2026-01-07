package ru.gfastg98.qr_scanner_compose.presentation.qr_result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.BuildConfig
import ru.gfastg98.qr_scanner_compose.R
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.data.entity.readQrCodeEntity
import ru.gfastg98.qr_scanner_compose.domain.utils.processQRCodeInfo
import ru.gfastg98.qr_scanner_compose.presentation.BaseViewModel
import ru.gfastg98.qr_scanner_compose.presentation.utils.provideUri
import ru.gfastg98.qr_scanner_compose.presentation.utils.showToast
import java.io.File

class QrResultViewModel(
    val application: Context,
    val intent: Intent,
    private val db: AppDatabase,
) : BaseViewModel<QrResultState, QrResultEvent, QrResultAction>(QrResultState()) {
    private val clipboardManager = requireNotNull(application.getSystemService<ClipboardManager>())

    override fun onAction(action: QrResultAction) {
        when (action) {
            QrResultAction.Save -> handleSaveAction()
            QrResultAction.CopyImage -> handleCopyImage()
            QrResultAction.CopyText -> handleCopyText()
            QrResultAction.ShareImage -> handleShareWithPhotoAction()
            QrResultAction.ShareText -> handleShareWithTextAction()
            QrResultAction.Open -> handleOpenAction()
        }
    }

    init {
        initializeState()
    }

    private fun initializeState() {
        val f = application.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val filename = intent.getStringExtra("file_name") ?: "intent.png"
        val file = File("$f/QRCODES/$filename")

        val qrCodeEntity = intent.readQrCodeEntity(application)
        val barcodeInfo = qrCodeEntity.processQRCodeInfo()

        currentState.value = QrResultState(
            QrResultState.Status.READY,
            qrCodeEntity,
            barcodeInfo,
            file
        )
    }

    private fun handleShareWithTextAction() {
        val state = requireNotNull(currentState.value)
        val uri = application.provideUri(state.file)

        ShareCompat.IntentBuilder(application)
            .setChooserTitle(R.string.QrResult__share_text)
            .setText(state.qrCodeEntity.content)
            .setStream(uri)
            .setType(Intent.normalizeMimeType("text/plain"))
            .createChooserIntent()
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
            .also {
                if (it.resolveActivity(application.packageManager) != null)
                    application.startActivity(it)
                else application.showToast("Нет приложения для отправки")
            }
    }

    private fun handleShareWithPhotoAction() {
        val state = requireNotNull(state.value)
        val uri = application.provideUri(state.file)

        ShareCompat.IntentBuilder(application)
            .setChooserTitle(R.string.QrResult__share_image)
            .setText(state.qrCodeEntity.content)
            .setStream(uri)
            .setType("image/png")
            .createChooserIntent()
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
            .let {
                if (it.resolveActivity(application.packageManager) != null)
                    application.startActivity(it)
                else application.showToast("Нет приложения для отправки")
            }
    }

    private fun handleSaveAction() {
        val state = requireNotNull(currentState.value)
        viewModelScope.launch { db.qrCodeDao().insertAll(state.qrCodeEntity) }
        onEvent(QrResultEvent.Finish)
    }

    private fun handleCopyText() {
        val state = requireNotNull(currentState.value)
        val clipData = ClipData.newPlainText(
            "Текст QR-кода",
            state.qrCodeEntity.content
        )
        clipboardManager.setPrimaryClip(clipData)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            application.showToast("Текст скопирован")
    }

    private fun handleCopyImage() {
        val state = requireNotNull(currentState.value)

        val uri = FileProvider.getUriForFile(
            application,
            "${BuildConfig.APPLICATION_ID}.provider",
            state.file
        )

        clipboardManager.setPrimaryClip(
            ClipData.newUri(
                application.contentResolver,
                "Скопированный QR код",
                uri
            )
        )

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            application.showToast("Картинка скопирована")
    }

    private fun handleOpenAction() {
        val state = requireNotNull(currentState.value)

        val data = when (val info = state.barcodeInfo) {
            is Barcode.UrlBookmark -> info.url!!.toUri()
            is Barcode.GeoPoint -> "geo:${info.lat},${info.lng}?q=${info.lat},${info.lng}".toUri()
            else -> null
        }

        application.startActivity(
            Intent(Intent.ACTION_VIEW).setData(data)
        )
    }
}