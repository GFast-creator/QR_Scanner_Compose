package ru.gfastg98.qr_scanner_compose.domain.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow

fun <T> SharedFlow<T>.throttleFist(windowDuration: Long): Flow<T> = flow {
    var windowStartTime = System.currentTimeMillis()
    var emitted = false
    collect { value ->
        val currentTime = System.currentTimeMillis()
        val delta = currentTime - windowStartTime
        if (delta >= windowDuration) {
            windowStartTime += windowDuration
            emitted = false
        }
        if (!emitted) {
            emit(value)
            emitted = true
        }
    }
}