package ru.gfastg98.qr_scanner_compose.domain.utils

import android.util.Patterns
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.GeoPoint
import com.google.mlkit.vision.barcode.common.Barcode.UrlBookmark
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity

fun QRCodeEntity.processQRCodeInfo(): Any? = barcodeObjectJson.let {
    val i = codeFormat
    when (i) {
        Barcode.TYPE_CONTACT_INFO -> Gson().fromJson(
            it,
            Barcode.ContactInfo::class.java
        )

        Barcode.TYPE_WIFI -> Gson().fromJson(it, Barcode.WiFi::class.java)
        Barcode.TYPE_PHONE -> Gson().fromJson(it, Barcode.Phone::class.java)
        Barcode.TYPE_URL -> Gson().fromJson(it, UrlBookmark::class.java)
        Barcode.TYPE_EMAIL -> Gson().fromJson(it, Barcode.Email::class.java)
        Barcode.TYPE_GEO -> Gson().fromJson(it, GeoPoint::class.java)
        Barcode.TYPE_CALENDAR_EVENT -> Gson().fromJson(it, Barcode.CalendarEvent::class.java)

        else -> {
            val list =
                Regex(Patterns.WEB_URL.pattern())
                    .findAll(content).toList()

            if (list.isNotEmpty())
                UrlBookmark("Web Url", list.first().value)
            else null
        }
    }
}