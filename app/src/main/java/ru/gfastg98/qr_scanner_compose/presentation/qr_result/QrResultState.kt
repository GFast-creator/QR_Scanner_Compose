package ru.gfastg98.qr_scanner_compose.presentation.qr_result

data class QrResultState(
    val status: Status,
) {
    enum class Status {
        INITIALIZE,
        READY
    }
}
