package ru.gfastg98.qr_scanner_compose.presentation.components.tip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TipState(
    private val coroutineScope: CoroutineScope,
) {
    var isVisible by mutableStateOf(false)
    private var job: Job? = null

    fun showToolTip() {
        job?.cancel()
        isVisible = true

        job = coroutineScope.launch(Dispatchers.IO) {
            delay(3000)
            hideTooltip()
        }
    }

    fun hideTooltip() {
        isVisible = false
        job?.cancel()
    }
}

@Composable
fun rememberTipState(): TipState {
    val scope = rememberCoroutineScope()
    return remember { TipState(scope) }
}