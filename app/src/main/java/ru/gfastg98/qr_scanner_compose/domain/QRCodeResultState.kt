package ru.gfastg98.qr_scanner_compose.domain

import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import java.io.File

data class QRCodeResultState(
    val qrCodeEntity: QRCodeEntity,
    val barcodeInfo: Any? = null,
    val file: File
)
