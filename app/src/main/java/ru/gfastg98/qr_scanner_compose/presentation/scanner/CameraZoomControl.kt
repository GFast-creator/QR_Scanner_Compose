package ru.gfastg98.qr_scanner_compose.presentation.scanner

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val zooms = listOf(0.5f, 1f, 2f, 5f, 10f)
private val TAG = "CameraZoomControl"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraZoomControl(
    modifier: Modifier = Modifier,
    selectedZoom: Float = 1f,
    onCheckedZoom: (Float) -> Unit,
    maxZoom: () -> Float = { 1f },
    minZoom: () -> Float = { 1f },
) {
    val availableZooms = remember { mutableStateListOf(*zooms.toTypedArray()) }

    LaunchedEffect(maxZoom(), minZoom()) {
        val maxZoom = (maxZoom() * 10).roundToInt() / 10.0
        val minZoom = (minZoom() * 10).roundToInt() / 10.0
        Log.i(TAG, "new max is ${maxZoom()}")
        Log.i(TAG, "new min is ${minZoom()}")
        availableZooms.clear()
        availableZooms.addAll(zooms.filter { it in minZoom..maxZoom })
    }

    Row(
        Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        availableZooms.forEachIndexed { index, item ->
            ToggleButton(
                checked = ((selectedZoom * 10f).roundToInt() / 10f) == item,
                onCheckedChange = { onCheckedZoom(item) },
                modifier = Modifier
                    .semantics { role = Role.RadioButton }
                    .wrapContentWidth(),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    availableZooms.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(item.toString())
            }
        }
    }
}