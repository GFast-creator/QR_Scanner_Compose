package ru.gfastg98.qr_scanner_compose.domain.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(content: String) {
    Toast.makeText(this, content, Toast.LENGTH_LONG).show()
}

fun Context.showToast(@StringRes id: Int) {
    Toast.makeText(this, id, Toast.LENGTH_LONG).show()
}