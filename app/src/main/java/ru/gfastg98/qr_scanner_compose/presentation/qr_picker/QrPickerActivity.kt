package ru.gfastg98.qr_scanner_compose.presentation.qr_picker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class QrPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QrPickerScreen() }
    }
}

