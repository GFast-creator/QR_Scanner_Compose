package ru.gfastg98.qr_scanner_compose.domain

import android.content.Context
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.domain.utils.showBitmapOnActivity

class QRCodeDatabaseViewModel(
    val database: AppDatabase
) : ViewModel() {
    val dao = database.qrCodeDao()
    val table = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(15000), emptyList())

    fun queryTable(generated: Boolean) = dao.getAllWithGenerated(generated)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(15000), emptyList())

    fun deleteAll(list: List<QRCodeEntity>) =
        viewModelScope.launch { database.qrCodeDao().deleteAll(list) }

    fun fullView(
        context: Context,
        item: QRCodeEntity
    ) {
        val bitmap = item.bitmap.decodeToImageBitmap().asAndroidBitmap()
        showBitmapOnActivity(
            context,
            bitmap,
            IntRect(IntOffset(0, 0), IntOffset(bitmap.width, bitmap.height)),
            item
        )
        /*if (
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
        }*/
    }
}