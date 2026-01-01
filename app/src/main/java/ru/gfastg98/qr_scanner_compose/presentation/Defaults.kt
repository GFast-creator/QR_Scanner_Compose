package ru.gfastg98.qr_scanner_compose.presentation

import android.content.Context
import android.os.Environment

object Defaults {
    const val QRCODE_INTENT_PATH = "/QRCODES/intent.png"
    fun getBitmapIntentPath(context: Context) =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path + QRCODE_INTENT_PATH
}