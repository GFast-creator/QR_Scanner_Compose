package ru.gfastg98.qr_scanner_compose.presentation.qr_result

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

private val TAG = QrResultActivity::class.java.simpleName

class QrResultActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CODE_FORMAT = "code_format"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRScannerTheme {
                QrResultScreen()
            }
        }
    }
}

