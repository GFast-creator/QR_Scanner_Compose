package ru.gfastg98.qr_scanner_compose

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.GeoPoint
import com.google.mlkit.vision.barcode.common.Barcode.UrlBookmark
import ru.gfastg98.qr_scanner_compose.ui.theme.QR_scanner_composeTheme
import java.io.ByteArrayOutputStream
import java.io.File


class QRResultActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QR_scanner_composeTheme {
                QRCodeViewer(intent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRCodeViewer(intent: Intent) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = context.getSystemService(
        ComponentActivity.CLIPBOARD_SERVICE
    ) as ClipboardManager
    val dbHelper = DBHelper(context)

    val config = LocalConfiguration.current

    if (intent.action in listOf(Intent.ACTION_SEND, Intent.ACTION_SENDTO)){
        Log.e("kilo", intent.type?:"null")
    }

    val f = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val filename = intent.getStringExtra("file_name") ?: "intent.png"

    
//            if (intent.action == Intent.ACTION_SEND) {
//            } else {
    val bitmap = BitmapFactory.decodeFile(
        f.toString()
                + "/QRCODES/$filename"
    )
    Log.e("kilo", f.toString())
    val content = intent.getStringExtra("content") ?: ""
    val barcode_obj_js = intent.getStringExtra("barcode_obj") ?: ""
    Log.e("kilo oo", barcode_obj_js)

    val uri = FileProvider.getUriForFile(
        context,
        "ru.gfastg98.qr_scanner_compose.provider",
        File(f!!.path + "/QRCODES/$filename")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("QR Code Result")
                },
                actions = {
                    IconButton(onClick = {
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND)
                                .apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TEXT, content)
                                    type = "*/*"
                                },
                            "Отправить QR-код"
                        ).also {
                            if (it.resolveActivity(context.packageManager) != null)
                                context.startActivity(it)
                        }
                    }) {
                        Icon(Icons.Default.TurnRight, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {

            val barcodeInfo =
                barcode_obj_js.let {
                    val i = intent.getIntExtra("code_format", -1)
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
                        Barcode.TYPE_CALENDAR_EVENT -> Gson().fromJson(
                            it,
                            Barcode.CalendarEvent::class.java
                        )

                        else -> {
                            val list = Regex(Patterns.WEB_URL.pattern()).findAll(content).toList()

                            if (list.isNotEmpty())
                                UrlBookmark("Web Url", list.first().value)
                            else null
                        }
                    }
                }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "mainImage",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(config.screenWidthDp.dp)
                )

                Box(
                    Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SelectionContainer {
                        Text(
                            when (barcodeInfo) {
                                is GeoPoint -> "Точка на карте\nКоординаты: ${barcodeInfo.lng}, ${barcodeInfo.lat}"
                                else -> content
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(modifier = Modifier
                        .weight(1f),
                        shape = RoundedCornerShape(10),
                        onClick = {
                            intent.getStringExtra("content")?.let {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "Текст QR-кода",
                                        it
                                    )
                                )
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                                    Toast.makeText(
                                        context,
                                        "Скопировано",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        }) {
                        Text("Скопировать текст")
                        Spacer(modifier = Modifier.size(10.dp))
                        Icon(
                            imageVector = Icons.Outlined.TextFields,
                            contentDescription = "Текст"
                        )
                    }

                    Button(
                        shape = RoundedCornerShape(10),
                        onClick = {
                            clipboardManager.setPrimaryClip(
                                ClipData.newUri(
                                    context.contentResolver,
                                    "QR code",
                                    uri
                                )
                            )
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                                Toast.makeText(
                                    context,
                                    "Картинка скопирована",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "QR код"
                        )
                    }
                }

                if (!intent.getBooleanExtra("view", false))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10),
                        onClick = {
                            val db = dbHelper.writableDatabase

                            db?.insert(
                                DBHelper.Contract.QRCodeEntry.TABLE_NAME,
                                null,
                                ContentValues().apply {
                                    val stream = ByteArrayOutputStream()
                                    bitmap.compress(
                                        Bitmap.CompressFormat.PNG,
                                        100,
                                        stream
                                    )
                                    put("bitmap", stream.toByteArray())
                                    put("content", content)
                                    put(
                                        "generated",
                                        intent.getBooleanExtra("generated", false)
                                    )
                                    put(
                                        "barcode_obj_js",
                                        barcode_obj_js
                                    )
                                }
                            )
                            (context as Activity).finish()
                        }) {

                        Text("Сохранить в галерее и закрыть")
                    }

                if (barcodeInfo != null) {
                    Button(modifier = Modifier
                        .fillMaxWidth(),
                        shape = RoundedCornerShape(10),
                        onClick = {
                            when (barcodeInfo) {
                                is UrlBookmark -> {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse(barcodeInfo.url!!))
                                    )
                                }

                                is GeoPoint -> {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW)
                                            .setData(
                                                Uri.parse(
                                                    "geo:${barcodeInfo.lat},${barcodeInfo.lng}?q=${barcodeInfo.lat},${barcodeInfo.lng}"
                                                )
                                            )
                                    )
                                }
                            }
                        }) {
                        Text(
                            when (barcodeInfo) {
                                is UrlBookmark -> "Открыть ссылку"
                                is GeoPoint -> "Открыть карту"
                                else -> ""
                            }
                        )
                    }
                }
            }
        }
    }
}

