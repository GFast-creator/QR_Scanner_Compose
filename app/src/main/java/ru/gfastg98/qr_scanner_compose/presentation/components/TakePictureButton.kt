package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize

class TakePictureButtonState private constructor() {
    var isTouched: Boolean by mutableStateOf(false)
    var onClickListener: (TakePictureButtonState) -> Unit by mutableStateOf({})

    constructor(onClick: (TakePictureButtonState) -> Unit) : this() {
        onClickListener = onClick
    }

    fun takePicture() {
        isTouched = !isTouched
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
    state: TakePictureButtonState = takePictureButtonState(),
) {
    val firstColor = MaterialTheme.colorScheme.primary
    var size by remember { mutableStateOf(Size.Zero) }
    val animatedSize by animateSizeAsState(
        if (state.isTouched) Size(
            size.minDimension / 2f,
            size.minDimension / 2f
        ) else Size(
            size.minDimension / 1.5f,
            size.minDimension / 1.5f
        )
    )
    val animatedRadius by animateFloatAsState(
        if (state.isTouched) size.minDimension / 12f else size.minDimension / 1.5f / 2f
    )
    val animatedColor by animateColorAsState(
        if (state.isTouched) Color.Red
        else firstColor
    )
    val interactableSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier then Modifier
            .clickable(
                interactionSource = interactableSource,
                indication = null,
                onClick = state::takePicture
            )
            .aspectRatio(1f)
            .onPlaced {
                size = it.size.toSize()
            }
            .drawWithCache {
                onDrawBehind {
                    drawCircle(
                        Color.White,
                        radius = size.minDimension / 2f - 15f,
                        style = Stroke(size.minDimension / 24f),
                        colorFilter = ColorFilter.tint(firstColor)
                    )

                    drawRoundRect(
                        color = animatedColor,
                        topLeft = Offset(
                            size.minDimension / 2f - animatedSize.minDimension / 2f,
                            size.minDimension / 2f - animatedSize.minDimension / 2f
                        ),
                        size = animatedSize,
                        cornerRadius = CornerRadius(animatedRadius)
                    )
                }
            }
    )
}

@Preview
@Composable
private fun TakePictureButtonPreview() {
    TakePictureButton(state = takePictureButtonState {})
}