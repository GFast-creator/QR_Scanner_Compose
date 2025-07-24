package ru.gfastg98.qr_scanner_compose.domain

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGSaver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.QRResultActivity
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity

class QRCodeDatabaseViewModel(
    val database: AppDatabase
) : ViewModel() {
    fun queryTable(generated: Boolean) =
        database.qrCodeDao()
            .getAllWithGenerated(generated)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(15000), emptyList())

    fun deleteAll(list: List<QRCodeEntity>) =
        viewModelScope.launch { database.qrCodeDao().deleteAll(list) }

    fun fullView(
        context: Context,
        item: QRCodeEntity
    ) {
        if (
            QRGSaver().save(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                    .path + "/QRCODES/",
                "intent",
                BitmapFactory.decodeByteArray(item.bitmap, 0, item.bitmap.size),
                QRGContents.ImageType.IMAGE_PNG
            )
        ) {
            context.startActivity(
                Intent(
                    context,
                    QRResultActivity::class.java
                ).putExtra("file_name", "intent.png")
                    .putExtra("content", item.content)
                    .putExtra("view", true)
                    .putExtra("barcode_obj", item.barcodeObjectJson)
                    .putExtra("code_format", item.codeFormat)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            Toast.makeText(context, "Ошибка при сохранении QR-кода", Toast.LENGTH_LONG).show()
        }
    }
}