package ru.gfastg98.qr_scanner_compose.presentation.main

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.presentation.BaseViewModel
import ru.gfastg98.qr_scanner_compose.presentation.main.MainScreenState.Status
import ru.gfastg98.qr_scanner_compose.presentation.utils.showBitmapOnActivity

class MainScreenViewModel(
    val application: Context,
    val database: AppDatabase,
) : BaseViewModel<MainScreenState, MainScreenEvent, MainScreenAction>(MainScreenState()) {
    private val dao = database.qrCodeDao()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            currentState.update {
                it.copy(
                    status = Status.READY,
                    table = dao.getAll()
                )
            }
        }
    }

    fun queryTable(generated: Boolean) = dao.getAllWithGenerated(generated)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(15000), emptyList())

    fun deleteAll(list: List<QRCodeEntity>) =
        viewModelScope.launch { database.qrCodeDao().deleteAll(list) }

    override fun onAction(action: MainScreenAction) {
        when (action) {
            MainScreenAction.ToGenerator -> onEvent(MainScreenEvent.NavigateToGenerator)
            MainScreenAction.ToScanner -> onEvent(MainScreenEvent.NavigateToScanner)
            is MainScreenAction.Select -> currentState.update { it.copy(selected = it.selected + action.qrcode) }
            is MainScreenAction.Unselect -> currentState.update { it.copy(selected = it.selected - action.qrcode) }
            is MainScreenAction.SelectAll -> currentState.update { it.copy(selected = it.table) }
            is MainScreenAction.UnselectAll -> currentState.update { it.copy(selected = emptyList()) }
            MainScreenAction.DeleteSelected -> {
                deleteAll(state.value.selected)
                currentState.update { it.copy(selected = emptyList()) }
            }

            is MainScreenAction.Open -> fullView(application, action.qrcode)
        }
    }

    fun fullView(
        context: Context,
        item: QRCodeEntity,
    ) {
        val bitmap = BitmapFactory.decodeByteArray(item.bitmap, 0, item.bitmap.size)
        showBitmapOnActivity(
            context,
            bitmap,
            IntRect(IntOffset(0, 0), IntOffset(bitmap.width, bitmap.height)),
            item,
            true
        )
    }
}