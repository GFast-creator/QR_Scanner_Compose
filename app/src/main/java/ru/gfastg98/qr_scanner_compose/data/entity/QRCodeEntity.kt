package ru.gfastg98.qr_scanner_compose.data.entity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.runtime.Stable
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity.Companion.TABLE_NAME
import ru.gfastg98.qr_scanner_compose.presentation.qr_result.QrResultActivity
import java.io.ByteArrayOutputStream
import java.io.File

@Stable
@Entity(tableName = TABLE_NAME)
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val bitmap: ByteArray = byteArrayOf(),
    val content: String = "",
    val generated: Boolean = false,
    @ColumnInfo(name = "barcode_obj_js") val barcodeObjectJson: String = "",
    @ColumnInfo(name = "code_format") val codeFormat: Int = -1,
) {
    companion object {
        const val TABLE_NAME = "qr_code_table"
        val EMPTY_ENTITY = QRCodeEntity()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QRCodeEntity

        if (uid != other.uid) return false
        if (generated != other.generated) return false
        if (codeFormat != other.codeFormat) return false
        if (!bitmap.contentEquals(other.bitmap)) return false
        if (content != other.content) return false
        if (barcodeObjectJson != other.barcodeObjectJson) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + generated.hashCode()
        result = 31 * result + codeFormat.hashCode()
        result = 31 * result + bitmap.contentHashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + barcodeObjectJson.hashCode()
        return result
    }
}

fun Intent.readQrCodeEntity(context: Context): QRCodeEntity {
    val f = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val filename = getStringExtra("file_name") ?: "intent.png"
    val file = File("$f/QRCODES/$filename")

    return QRCodeEntity(
        bitmap = file.readBytes(),
        content = getStringExtra("content") ?: "",
        generated = getBooleanExtra("generated", false),
        barcodeObjectJson = getStringExtra("barcode_obj") ?: "",
        codeFormat = getIntExtra(QrResultActivity.EXTRA_CODE_FORMAT, -1)
    )
}

val mockQRCodeEntity = QRCodeEntity(
    uid = 0,
    bitmap = createBitmap(300, 300).applyCanvas {
        drawRect(
            android.graphics.Rect(0, 0, width, height),
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
            }
        )
        drawCircle(
            150f,
            150f,
            150f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLUE
            }
        )
    }.let {
        val output = ByteArrayOutputStream()
        it.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.toByteArray()
    },
    content = "test",
    generated = true,
    barcodeObjectJson = "{}",
    codeFormat = 1,
)