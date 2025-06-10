package ru.gfastg98.qr_scanner_compose

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import ru.gfastg98.qr_scanner_compose.fragments.showBitmapOnActivity
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

class QRPickerActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var barcodes by remember {
                mutableStateOf(emptyList<Barcode>())
            }

            val bitmap: Bitmap? = intent.getStringExtra("bitmap")?.run {
                BitmapFactory.decodeFile(
                    getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                    )!!.path + "/QRCODES/intent.png"
                )
            }

            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
            )
            bitmap?.let { b ->
                scanner.process(
                    b, 0
                ).addOnCompleteListener { task ->
                    val x = task.result
                    if (x.isNotEmpty()) {
                        barcodes = x.sortedBy { it.boundingBox!!.top }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Нет QR кодов на выбранной картинке",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                    }
                }
            }

            QRScannerTheme {
                if (barcodes.isNotEmpty())
                    Scaffold { innerPadding ->

                        val context = LocalContext.current
                        var pickedRes by remember {
                            mutableIntStateOf(0)
                        }

                        Column(
                            Modifier.padding(innerPadding)
                        ) {
                            if (barcodes.isNotEmpty()) {
                                val bitmapView = bitmap?.copy(
                                    Bitmap.Config.ARGB_8888,
                                    true
                                )

                                Image(
                                    modifier = Modifier.fillMaxWidth(),
                                    bitmap = bitmapView?.also {
                                        val canvas = Canvas(it)
                                        barcodes.forEachIndexed { index, barcode ->
                                            canvas.drawPath(
                                                Path()
                                                    .apply {
                                                        val points = barcode.cornerPoints!!
                                                        moveTo(
                                                            points[0].x.toFloat(),
                                                            points[0].y.toFloat()
                                                        )
                                                        lineTo(
                                                            points[1].x.toFloat(),
                                                            points[1].y.toFloat()
                                                        )
                                                        lineTo(
                                                            points[2].x.toFloat(),
                                                            points[2].y.toFloat()
                                                        )
                                                        lineTo(
                                                            points[3].x.toFloat(),
                                                            points[3].y.toFloat()
                                                        )
                                                        close()
                                                    },
                                                Paint()
                                                    .apply {
                                                        color =
                                                            if (index != pickedRes)
                                                                Color.BLUE
                                                            else
                                                                Color.RED
                                                        style = Paint.Style.STROKE
                                                        strokeWidth = 20f
                                                    }
                                            )
                                        }
                                    }!!.asImageBitmap(),
                                    contentDescription = null
                                )
                            }
                            Column {
                                Row {
                                    IconButton(
                                        enabled = pickedRes != 0,
                                        modifier = Modifier
                                            .weight(0.5f),
                                        onClick = { pickedRes-- }
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, null)
                                    }
                                    IconButton(
                                        enabled = pickedRes != barcodes.size - 1,
                                        modifier = Modifier
                                            .weight(0.5f),
                                        onClick = { pickedRes++ }
                                    ) {
                                        Icon(Icons.Default.ChevronRight, null)
                                    }
                                }
                                Button(modifier = Modifier
                                    .fillMaxWidth(),
                                    shape = RoundedCornerShape(10),
                                    onClick = {
                                        showBitmapOnActivity(
                                            bitmap!!,
                                            barcodes[pickedRes],
                                            context
                                        )
                                    }) {
                                    Text("Сохранить выбранное и закрыть")
                                }
                            }
                        }
                    }
            }
        }
    }
}