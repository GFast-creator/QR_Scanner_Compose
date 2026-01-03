package ru.gfastg98.qr_scanner_compose.presentation.qr_result

sealed interface QrResultAction {
    data object Save : QrResultAction
    data object ShareText : QrResultAction
    data object ShareImage : QrResultAction
    data object CopyText : QrResultAction
    data object CopyImage : QrResultAction
    data object Open : QrResultAction
}