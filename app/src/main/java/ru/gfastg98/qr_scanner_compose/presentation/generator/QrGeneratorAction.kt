package ru.gfastg98.qr_scanner_compose.presentation.generator

sealed interface QrGeneratorAction {
    data class NewPrompt(val string: String) : QrGeneratorAction
    data object Continue : QrGeneratorAction
}