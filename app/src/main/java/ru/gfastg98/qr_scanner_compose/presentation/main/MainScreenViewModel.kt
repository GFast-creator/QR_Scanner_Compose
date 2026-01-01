package ru.gfastg98.qr_scanner_compose.presentation.main

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
import ru.gfastg98.qr_scanner_compose.presentation.utils.showBitmapOnActivity

class MainScreenViewModel(
    val database: AppDatabase,
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
        item: QRCodeEntity,
    ) {
        val bitmap = item.bitmap.decodeToImageBitmap().asAndroidBitmap()
        showBitmapOnActivity(
            context,
            bitmap,
            IntRect(IntOffset(0, 0), IntOffset(bitmap.width, bitmap.height)),
            item,
            true
        )
    }
}