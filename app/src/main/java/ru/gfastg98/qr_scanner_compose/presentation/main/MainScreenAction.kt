package ru.gfastg98.qr_scanner_compose.presentation.main

import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity

sealed interface MainScreenAction {
    data object ToGenerator : MainScreenAction
    data object ToScanner : MainScreenAction
    data class Select(val qrcode: QRCodeEntity) : MainScreenAction
    data class Unselect(val qrcode: QRCodeEntity) : MainScreenAction
    data object DeleteSelected : MainScreenAction
    data object SelectAll : MainScreenAction
    data object UnselectAll : MainScreenAction
    data class Open(val qrcode: QRCodeEntity) : MainScreenAction
}