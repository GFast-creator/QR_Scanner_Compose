package ru.gfastg98.qr_scanner_compose.domain.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toComposeRect
import com.google.mlkit.vision.barcode.common.Barcode

fun Barcode.resultRect(): Rect {
    return boundingBox!!.toComposeRect()
    /*
    val start = bounds.minOf { it.x } - 10
    val end = bounds.maxOf { it.x } + 10

    val top = bounds.minOf { it.y } - 10
    val bottom = bounds.maxOf { it.y } + 10

    return Rect(start.toFloat(), top.toFloat(), end.toFloat(), bottom.toFloat())*/
}

fun Rect.translateToComponentsSize(): Rect {
    return this
}

fun Rect.translate(givenSize: Size, targetSize: Size): Rect {
    val xTranslate = targetSize.width / givenSize.width
    val yTranslate = targetSize.height / givenSize.height
    return Rect(
        left * xTranslate,
        top * yTranslate,
        right * xTranslate,
        bottom * yTranslate,
    )
}