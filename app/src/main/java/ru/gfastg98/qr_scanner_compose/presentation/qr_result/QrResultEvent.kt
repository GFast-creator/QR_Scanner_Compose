package ru.gfastg98.qr_scanner_compose.presentation.qr_result

sealed interface QrResultEvent {
    data object Finish : QrResultEvent
}