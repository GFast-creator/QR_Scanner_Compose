package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Stable
class FloatingActinBottomBarState {
    var scrolled: ScrollDirections by mutableStateOf(ScrollDirections.Idle)

    sealed interface ScrollDirections {
        data object Up : ScrollDirections
        data object Down : ScrollDirections
        data object Idle : ScrollDirections
    }
}

@Composable
fun rememberFloatingActionBottomBarState() = remember {
    FloatingActinBottomBarState()
}

@Composable
fun FloatingActionBottomBar(
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    state: FloatingActinBottomBarState = rememberFloatingActionBottomBarState(),
    content: List<@Composable RowScope.(FloatingActinBottomBarState) -> Unit>,
) {
    Row {
        content.forEach {
            Box(
                modifier =
                    Modifier.defaultMinSize(
                        minWidth = 56.dp,
                        minHeight = 56.dp,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                it.invoke(this@Row, state)
            }
        }
    }
}