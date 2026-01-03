package ru.gfastg98.qr_scanner_compose.presentation.state

import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import java.io.File

data class QRCodeResultState(
    val status: Status = Status.INITIALIZING,
    val qrCodeEntity: QRCodeEntity = QRCodeEntity.EMPTY_ENTITY,
    val barcodeInfo: Any? = null,
    val file: File = File(""),
) {
    enum class Status {
        INITIALIZING,
        READY
    }

    fun isReady() = status == Status.READY
}

