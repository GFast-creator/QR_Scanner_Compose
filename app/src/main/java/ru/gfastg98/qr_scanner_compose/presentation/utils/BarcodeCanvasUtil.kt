package ru.gfastg98.qr_scanner_compose.presentation.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.graphics.applyCanvas
import com.google.mlkit.vision.barcode.common.Barcode

private val path = Path()
private val paint = Paint().apply {
    style = Paint.Style.STROKE
    strokeWidth = 20f
}

fun Bitmap.drawBarcodes(
    barcodes: List<Barcode>, selectedIndex: Int,
) = applyCanvas { drawBarcodes(barcodes, selectedIndex) }

fun Canvas.drawBarcodes(
    barcodes: List<Barcode>, selectedIndex: Int,
) {
    barcodes.fastForEachIndexed { index, item ->
        drawBarcode(item, index == selectedIndex)
    }
}

fun Canvas.drawBarcode(
    barcode: Barcode, isFirst: Boolean,
) {
    drawPath(
        path.apply {
            reset()
            val points = barcode.cornerPoints!!
            moveTo(
                points[0].x.toFloat(),
                points[0].y.toFloat()
            )
            lineTo(
                points[1].x.toFloat(),
                points[1].y.toFloat()
            )
            lineTo(
                points[2].x.toFloat(),
                points[2].y.toFloat()
            )
            lineTo(
                points[3].x.toFloat(),
                points[3].y.toFloat()
            )
            close()
        },
        paint.apply {
            color = if (isFirst) Color.BLUE else Color.RED
        }
    )
}