package ru.gfastg98.qr_scanner_compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DBGenShowFragment() {
    DatabaseTableScreen(generated = true)
}