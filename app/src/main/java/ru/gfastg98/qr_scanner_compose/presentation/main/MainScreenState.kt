package ru.gfastg98.qr_scanner_compose.presentation.main

import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity

data class MainScreenState(
    val status: Status = Status.INITIALIZING,
    val table: List<QRCodeEntity> = emptyList(),
    val selected: List<QRCodeEntity> = emptyList(),
) {
    enum class Status {
        INITIALIZING,
        READY
    }
}
