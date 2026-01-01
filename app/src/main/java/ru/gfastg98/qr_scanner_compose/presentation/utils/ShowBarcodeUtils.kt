package ru.gfastg98.qr_scanner_compose.presentation.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGSaver
import androidx.compose.ui.unit.IntRect
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.presentation.activity.QRResultActivity

private const val TAG = "BarcodeUtils"

fun showBitmapOnActivity(
    context: Context,
    bitmap: Bitmap,
    rect: IntRect,
    barcode: Barcode,
    isView: Boolean,
) {
    val resultBitmap = Bitmap.createBitmap(
        bitmap,
        (rect.left - 20).coerceAtLeast(0),
        (rect.top - 20).coerceAtLeast(0),
        (rect.width + 40).coerceAtMost(bitmap.width - rect.left),
        (rect.height + 40).coerceAtMost(bitmap.height - rect.top)
    )

    val filename = "intent"
    Log.i(
        TAG,
        if (QRGSaver().save(
                context.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES
                )!!.path + "/QRCODES/",
                filename,
                resultBitmap,
                QRGContents.ImageType.IMAGE_PNG
            )
        ) "saved" else "no save"
    )

    context.startActivity(
        Intent(context, QRResultActivity::class.java)
            .putExtra("file_name", "$filename.png")
            .putExtra("content", barcode.rawValue)
            .putExtra("generated", false)
            .apply {
                when (barcode.valueType) {
                    Barcode.TYPE_CONTACT_INFO -> Gson().toJson(barcode.contactInfo)
                    Barcode.TYPE_WIFI -> Gson().toJson(barcode.wifi)
                    Barcode.TYPE_PHONE -> Gson().toJson(barcode.phone)
                    Barcode.TYPE_URL -> Gson().toJson(barcode.url)
                    Barcode.TYPE_EMAIL -> Gson().toJson(barcode.email)
                    Barcode.TYPE_GEO -> Gson().toJson(barcode.geoPoint)
                    Barcode.TYPE_CALENDAR_EVENT -> Gson().toJson(barcode.calendarEvent)
                    else -> null
                }?.let { obj ->
                    putExtra("barcode_obj", obj)
                    Log.e(TAG, obj)
                }
            }
            .putExtra(QRResultActivity.EXTRA_CODE_FORMAT, barcode.valueType)
            .putExtra("view", isView)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun showBitmapOnActivity(
    applicationContext: Context,
    bitmap: Bitmap,
    rect: IntRect,
    barcode: QRCodeEntity,
    isView: Boolean,
) {
    val resultBitmap = Bitmap.createBitmap(
        bitmap,
        (rect.left - 20).coerceAtLeast(0),
        (rect.top - 20).coerceAtLeast(0),
        (rect.width + 40).coerceAtMost(bitmap.width - rect.left),
        (rect.height + 40).coerceAtMost(bitmap.height - rect.top)
    )

    val saveResult = QRGSaver().save(
        applicationContext.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )!!.path + "/QRCODES/",
        "intent",
        resultBitmap,
        QRGContents.ImageType.IMAGE_PNG
    )

    if (!saveResult) {
        Log.i(TAG, "not saved")
        return
    }

    Log.e(TAG, barcode.barcodeObjectJson)

    applicationContext.startActivity(
        Intent(applicationContext, QRResultActivity::class.java)
            .putExtra("file_name", "intent.png")
            .putExtra("content", barcode.content)
            .putExtra("generated", barcode.generated)
            .putExtra("barcode_obj", barcode.barcodeObjectJson)
            .putExtra(QRResultActivity.EXTRA_CODE_FORMAT, barcode.codeFormat)
            .putExtra("view", isView)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

