package ru.gfastg98.qr_scanner_compose.presentation.utils

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

fun Context.provideUri(file: File) = FileProvider.getUriForFile(
    this,
    "${packageName}.provider",
    file
)
