package ru.gfastg98.qr_scanner_compose.domain.utils

import android.graphics.Bitmap
import androidx.core.graphics.rotationMatrix

fun Bitmap.rotate(degree: Int) = Bitmap.createBitmap(
    this,
    0,
    0,
    width,
    height,
    rotationMatrix(degree.toFloat()),
    true
)
