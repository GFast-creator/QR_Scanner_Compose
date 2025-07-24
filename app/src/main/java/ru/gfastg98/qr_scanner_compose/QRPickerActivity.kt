package ru.gfastg98.qr_scanner_compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntRect
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import ru.gfastg98.qr_scanner_compose.domain.utils.showBitmapOnActivity
import ru.gfastg98.qr_scanner_compose.domain.utils.showToast
import ru.gfastg98.qr_scanner_compose.presentation.components.DETECTOR
import ru.gfastg98.qr_scanner_compose.presentation.components.Screen

class QRPickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRCodePickerScreen()
        }
    }
}

@Composable
private fun QRCodePickerScreen() = Screen {
    val activity = LocalActivity.current
    activity ?: return@Screen
    title = "QR Code Picker"

    navigationIconAction {
        activity.finish()
    }

    var barcodes by remember { mutableStateOf(emptyList<Barcode>()) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        bitmap = activity.intent?.getStringExtra("bitmap")?.run {
            BitmapFactory.decodeFile(
                activity.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES
                )!!.path + "/QRCODES/intent.png"
            )
        } ?: let {
            activity.finish()
            return@LaunchedEffect
        }

        DETECTOR.process(InputImage.fromBitmap(bitmap!!, 0))
            .addOnSuccessListener { detections ->
                if (detections.isNotEmpty()) {
                    barcodes = detections.sortedBy { it.boundingBox!!.top }
                } else {
                    activity.showToast("Нет QR кодов на выбранной картинке")
                    activity.finish()
                }
            }.addOnFailureListener {
                activity.showToast("Ошибка при обработке")
                activity.finish()
            }
    }

    content {
        if (barcodes.isNotEmpty()) {

            val context = LocalContext.current
            var pickedRes by remember {
                mutableIntStateOf(0)
            }

            Column {
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
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10),
                        onClick = {
                            showBitmapOnActivity(
                                context,
                                bitmap!!,
                                barcodes[pickedRes].boundingBox!!.let {
                                    IntRect(
                                        it.left,
                                        it.top,
                                        it.right,
                                        it.bottom
                                    )
                                },
                                barcodes[pickedRes]
                            )
                        }) {
                        Text("Сохранить выбранное и закрыть")
                    }
                }
            }
        }
    }
}
