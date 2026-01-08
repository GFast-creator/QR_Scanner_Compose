package ru.gfastg98.qr_scanner_compose.presentation.generator

import android.graphics.Bitmap

data class QrGeneratorState(
    val bitmap: Bitmap? = null,
    val content: String = "",
)
