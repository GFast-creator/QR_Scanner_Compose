package ru.gfastg98.qr_scanner_compose.presentation.qr_picker

sealed interface QrPickerScreenAction {
    data object Previous : QrPickerScreenAction
    data object Next : QrPickerScreenAction
    data object Close : QrPickerScreenAction
    data object Save : QrPickerScreenAction
}