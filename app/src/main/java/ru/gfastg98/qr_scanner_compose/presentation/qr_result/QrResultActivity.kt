package ru.gfastg98.qr_scanner_compose.presentation.qr_result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

private val TAG = QrResultActivity::class.java.simpleName

class QrResultActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_CODE_FORMAT = "code_format"
        const val EXTRA_GENERATED = "generated"
        const val EXTRA_BARCODE_OBJECT = "barcode_obj"
        const val EXTRA_VIEW = "view"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRScannerTheme {
                QrResultScreen()
            }
        }
    }

    class IntentBuilder(private val context: Context) {
        private val intent = Intent(
            context,
            QrResultActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        fun setFileName(file: String): IntentBuilder {
            intent.putExtra(EXTRA_FILE_NAME, file)
            return this
        }

        fun setContent(content: String): IntentBuilder {
            intent.putExtra(EXTRA_CONTENT, content)
            return this
        }

        fun setGenerated(generated: Boolean): IntentBuilder {
            intent.putExtra(EXTRA_GENERATED, generated)
            return this
        }

        fun setBarcodeObjectJson(barcodeObj: String): IntentBuilder {
            intent.putExtra(EXTRA_BARCODE_OBJECT, barcodeObj)
            return this
        }

        fun setCodeFormat(codeFormat: Int): IntentBuilder {
            intent.putExtra(EXTRA_CODE_FORMAT, codeFormat)
            return this
        }

        fun setIsView(isView: Boolean): IntentBuilder {
            intent.putExtra(EXTRA_VIEW, isView)
            return this
        }

        fun readBarcodeEntity(barcode: QRCodeEntity): IntentBuilder {
            return apply {
                setContent(barcode.content)
                setGenerated(barcode.generated)
                setBarcodeObjectJson(barcode.barcodeObjectJson)
                setCodeFormat(barcode.codeFormat)
            }
        }

        fun build(): Intent = intent
        fun launch() = context.startActivity(intent)
    }
}
