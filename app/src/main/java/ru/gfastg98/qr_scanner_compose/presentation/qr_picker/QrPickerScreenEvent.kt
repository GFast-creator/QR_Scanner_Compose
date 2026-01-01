package ru.gfastg98.qr_scanner_compose.presentation.qr_picker

sealed interface QrPickerScreenEvent {
    data object Finish : QrPickerScreenEvent
}
