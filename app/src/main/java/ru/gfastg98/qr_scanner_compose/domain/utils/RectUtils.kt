package ru.gfastg98.qr_scanner_compose.domain.utils

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toComposeRect
import com.google.mlkit.vision.barcode.common.Barcode

private const val TAG = "RectUtils"

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
    if (givenSize.width != 0f && givenSize.height != 0f) return this

    val xScale = targetSize.width / givenSize.width
    val yScale = targetSize.height / givenSize.height

    return Rect(
        left * xScale,
        top * yScale,
        right * xScale,
        bottom * yScale,
    )
}

fun Rect.mapUsingPreviewTransform(
    previewView: PreviewView,
): Rect {
    val matrix = android.graphics.Matrix()
    previewView.outputTransform?.matrix?.invert(matrix) ?: return this

    val rectF = android.graphics.RectF(
        left, top, right, bottom
    )

    matrix.mapRect(rectF)

    Log.i(TAG, "matrix: $matrix, rectF: $rectF")

    return Rect(
        rectF.left,
        rectF.top,
        rectF.right,
        rectF.bottom
    )
}