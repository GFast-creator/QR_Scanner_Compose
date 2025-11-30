package ru.gfastg98.qr_scanner_compose.presentation.components.tip

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

@Composable
internal fun Tooltip(
    text: String,
    isVisible: Boolean,
    targetBounds: Rect,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
) {
    var parentWidth = remember(targetBounds) {
        targetBounds.width
        +targetBounds.left
        +targetBounds.right
    }

    var localSize by remember { mutableStateOf(IntSize.Zero) }
    val finalOffset = remember(parentWidth, localSize) {
        (targetBounds.topCenter.round() - localSize.center.copy(y = 0) - IntOffset(
            0,
            localSize.height.toInt()
        ))
            .let {
                it.copy(x = it.x.coerceIn(0, (parentWidth.roundToInt() - localSize.width)))
            }
    }

    if (isVisible) {
        Popup(
            alignment = Alignment.TopStart,
            offset = finalOffset, // Смещение подсказки над компонентом
            properties = PopupProperties(focusable = false, clippingEnabled = false),
            onDismissRequest = onDismissRequest
        ) {
            Row(
                modifier = Modifier
                    .onGloballyPositioned {
                        localSize = it.size
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Gray
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun Modifier.tooltip(text: String): Modifier {
    return composed {
        val state = rememberTipState()
        var offset by remember { mutableStateOf(Offset.Zero) }
        var rect by remember { mutableStateOf(Rect.Zero) }
        Tooltip(
            text = text,
            isVisible = state.isVisible,
            targetBounds = rect,
            onDismissRequest = { state.hideTooltip() }
        )
        this
            .onPlaced {
                rect = it.boundsInParent()
                offset = it.positionInParent() + it.size.center.copy(y = 0).toOffset()
            }
            .combinedClickable {
                state.showToolTip()
            }
    }
}

// Пример использования
@Preview
@Composable
fun TooltipExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .tooltip("Это всплывающая подсказка!")
                .size(200.dp)
        ) {
            Text(text = "Нажми на меня")
        }
    }
}