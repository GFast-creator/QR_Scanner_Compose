package ru.gfastg98.qr_scanner_compose.presentation.utils

import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.min

fun Size.normalize(): Size = copy(max(width, height), min(width, height))
fun Size.toAspectRatio() = width / height