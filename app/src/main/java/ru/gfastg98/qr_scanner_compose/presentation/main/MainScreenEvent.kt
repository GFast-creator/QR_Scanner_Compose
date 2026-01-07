package ru.gfastg98.qr_scanner_compose.presentation.main

sealed interface MainScreenEvent {
    data object NavigateToGenerator : MainScreenEvent
    data object NavigateToScanner : MainScreenEvent
}