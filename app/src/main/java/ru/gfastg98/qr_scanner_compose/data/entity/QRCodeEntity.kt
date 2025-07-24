package ru.gfastg98.qr_scanner_compose.data.entity

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity.Companion.TABLE_NAME

@Stable
@Entity(tableName = TABLE_NAME)
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val bitmap: ByteArray,
    val content: String,
    val generated: Boolean,
    @ColumnInfo(name = "barcode_obj_js") val barcodeObjectJson: String,
    @ColumnInfo(name = "code_format") val codeFormat: Int,
) {
    companion object {
        const val TABLE_NAME = "qr_code_table"
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
        var result = uid
        result = 31 * result + generated.hashCode()
        result = 31 * result + codeFormat
        result = 31 * result + bitmap.contentHashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + barcodeObjectJson.hashCode()
        return result
    }
}