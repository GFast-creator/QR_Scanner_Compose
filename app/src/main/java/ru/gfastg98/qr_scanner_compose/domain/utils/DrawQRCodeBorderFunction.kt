package ru.gfastg98.qr_scanner_compose.domain.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Функция отрисовки обводки QR-кода
 */
private val path = Path()
fun ContentDrawScope.drawBarcodeSelection(rect: Rect, isScanning: Boolean) {
    val rad = 100.dp.toPx() * (rect.size.minDimension / 200.dp.toPx())
    20.dp

    if (isScanning) {
        path.apply {
            reset()
            // Добавляем внешний прямоугольник
            addRect(Rect(Offset.Zero, size))
            // Вырезаем внутренний прямоугольник со скруглениями
            op(
                Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect,
                            CornerRadius(rad.toDp().value, rad.toDp().value)
                        )
                    )
                },
                this,
                PathOperation.ReverseDifference
            )

            drawPath(
                path = this,
                color = Color.Black.copy(0.5f) // Цвет внешнего прямоугольника
            )
        }
    }

    path.apply {
        reset()
        moveTo(rect.topLeft + Offset(0f, rad / 2))
        arcTo(Rect(rect.topLeft, Size(rad, rad)), 180f, 90f, false)
        moveTo(rect.topRight + Offset(-rad / 2, 0f))
        arcTo(Rect(rect.topRight + Offset(-rad, 0f), Size(rad, rad)), -90f, 90f, false)
        moveTo(rect.bottomRight + Offset(0f, -rad / 2))
        arcTo(Rect(rect.bottomRight + Offset(-rad, -rad), Size(rad, rad)), 0f, 90f, false)
        moveTo(rect.bottomLeft + Offset(rad / 2, 0f))
        arcTo(Rect(rect.bottomLeft + Offset(0f, -rad), Size(rad, rad)), 90f, 90f, false)

        drawPath(
            this, Color.White,
            style = Stroke(
                width = 15f,
                cap = StrokeCap.Round
            )
        )
    }
}

@Preview
@Composable
private fun DrawBarcodeSelectionPreview() {
    Spacer(
        Modifier
            .background(Color.Gray)
            .drawWithContent {
                drawContent()
                val preview = size.copy(250f, 250f)
                drawBarcodeSelection(preview.toRect(), true)
            }
            .size(300.dp)
    )
}
