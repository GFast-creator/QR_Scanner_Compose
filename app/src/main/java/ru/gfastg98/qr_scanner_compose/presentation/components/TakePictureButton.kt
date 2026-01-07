package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.lerp

class TakePictureButtonState private constructor() {
    var isChecked: Boolean by mutableStateOf(false)
    var onClickListener: (TakePictureButtonState) -> Unit by mutableStateOf({})

    constructor(onClick: (TakePictureButtonState) -> Unit) : this() {
        onClickListener = onClick
    }

    fun takePicture() {
        isChecked = !isChecked
        onClickListener(this)
    }
}

@Composable
fun takePictureButtonState(
    onClick: (TakePictureButtonState) -> Unit = {},
) = remember { TakePictureButtonState(onClick) }

@Composable
fun TakePictureButton(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChanged: (newValue: Boolean) -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "progressAnimation"
    )

    val interactableSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                interactionSource = interactableSource,
                indication = null,
                onClick = { onCheckedChanged(checked.not()) }
            )
            .drawWithCache {
                val minDim = size.minDimension
                val outerRingRadius = minDim / 2f - 15f
                val strokeWidth = minDim / 24f

                val idleSize = minDim / 1.5f
                val recordSize = minDim / 2f
                val currentSize = lerp(idleSize, recordSize, progress)

                val idleRadius = idleSize / 2f
                val pressedRadius = minDim / 12f
                val currentCornerRadius = lerp(idleRadius, pressedRadius, progress)

                val color = lerp(primaryColor, Color.Red, progress)

                onDrawBehind {
                    drawCircle(
                        color = primaryColor,
                        radius = outerRingRadius,
                        style = Stroke(strokeWidth),
                    )

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(
                            x = center.x - currentSize / 2f,
                            y = center.y - currentSize / 2f
                        ),
                        size = Size(currentSize, currentSize),
                        cornerRadius = CornerRadius(currentCornerRadius)
                    )
                }
            }
    )
}

@Preview
@Composable
private fun TakePictureButtonPreview() {
    var checked by remember { mutableStateOf(false) }
    TakePictureButton(
        checked = checked,
        onCheckedChanged = { checked = it }
    )
}