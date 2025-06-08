package ru.gfastg98.qr_scanner_compose.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Environment
import android.util.Log
import android.view.ViewTreeObserver
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.gfastg98.qr_scanner_compose.QRResultActivity
import androidx.core.graphics.scale

private val TAG = "QRCodeGeneratorFragment"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun QRCodeGeneratorFragment() {
    Column {
        val context = LocalContext.current
        val isKeyboardOpen by keyboardAsState()
        val size by animateDpAsState(
            if (isKeyboardOpen == Keyboard.Opened) 200.dp else LocalConfiguration.current.screenWidthDp.dp,
            label = ""
        )
        var content by rememberSaveable {
            mutableStateOf("")
        }
        val modifier = Modifier
            .animateContentSize(tween(300))
            .size(size)
            .background(androidx.compose.ui.graphics.Color.White)
            .align(CenterHorizontally)

        val red = {
            QRGEncoder(content, null, QRGContents.Type.TEXT, 2)
                .let {
                    it.colorBlack = Color.WHITE
                    it.colorWhite = Color.BLACK

                    it.bitmap.scale(300, 300, false)
                }
        }
            if (content.isBlank())
                Image(
                    modifier = modifier,
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "blank",
                )
            else {
                Image(
                    modifier = modifier,
                    bitmap = red().asImageBitmap(),
                    contentDescription = "image of generated qrcode"
                )
            }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = content,
            onValueChange = { content = it }
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10),
            onClick = {
                val filename = "intent"
                Log.i(
                    TAG, if (QRGSaver().save(
                            context.getExternalFilesDir(
                                Environment.DIRECTORY_PICTURES
                            )!!.path + "/QRCODES/",
                            filename,
                            red(),
                            QRGContents.ImageType.IMAGE_PNG
                        )
                    ) "saved" else "no save"
                )

                context.startActivity(
                    Intent(
                        context,
                        QRResultActivity::class.java
                    )
                        .putExtra("file_name", "$filename.png")
                        .putExtra("content", content)
                        .putExtra("generated", true)
                        .also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                )
            },
            enabled = content.isNotBlank()
        ) {
            Text("Сохранить")
        }
    }
}

enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Keyboard> {
    val keyboardState = remember { mutableStateOf(Keyboard.Closed) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                Keyboard.Opened
            } else {
                Keyboard.Closed
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardState
}