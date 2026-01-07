package ru.gfastg98.qr_scanner_compose.presentation

import android.content.Context
import android.os.Environment

object Defaults {
    const val QRCODE_FILE_NAME = "intent.png"
    const val QRCODE_FOLDER_NAME = "QRCODES"
    const val QRCODE_INTENT_PATH = "/$QRCODE_FOLDER_NAME/$QRCODE_FILE_NAME"

    fun getBitmapIntentPath(context: Context) =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path + QRCODE_INTENT_PATH
}