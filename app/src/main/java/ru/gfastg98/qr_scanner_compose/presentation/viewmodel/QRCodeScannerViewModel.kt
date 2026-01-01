package ru.gfastg98.qr_scanner_compose.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGSaver
import androidx.lifecycle.ViewModel
import ru.gfastg98.qr_scanner_compose.presentation.qr_picker.QrPickerActivity

class QRCodeScannerViewModel : ViewModel() {
    fun processMedia(context: Context, uri: Uri?) {
        uri ?: return

        val pickedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    context.contentResolver,
                    uri
                )
            )
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        if (
            QRGSaver().save(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path + "/QRCODES/",
                "intent",
                pickedBitmap,
                QRGContents.ImageType.IMAGE_PNG
            )
        ) {
            context.startActivity(
                Intent(context, QrPickerActivity::class.java)
                    .putExtra("bitmap", "intent")
            )
        }

        pickedBitmap.recycle()
    }
}

