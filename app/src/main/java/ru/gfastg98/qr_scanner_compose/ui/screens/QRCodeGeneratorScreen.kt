package ru.gfastg98.qr_scanner_compose.ui.screens

import android.content.Intent
import android.graphics.Color
import android.os.Environment
import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import ru.gfastg98.qr_scanner_compose.QRResultActivity
import ru.gfastg98.qr_scanner_compose.ui.components.keyboardAsState

private val TAG = "QRCodeGeneratorFragment"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun QRCodeGeneratorScreen() {
    Column {
        val context = LocalContext.current
        val isKeyboardOpen by keyboardAsState()
        val size by animateDpAsState(
            if (isKeyboardOpen == Keyboard.Opened) 200.dp
            else LocalConfiguration.current.screenWidthDp.dp,
            label = "qr code preview size animation"
        )
        var content by rememberSaveable { mutableStateOf("") }

        val modifier = Modifier
            .animateContentSize(tween(300))
            .size(size)
            .background(androidx.compose.ui.graphics.Color.White)
            .align(CenterHorizontally)

        val render = remember {
            {
                QRGEncoder(content, null, QRGContents.Type.TEXT, 2).let {
                    it.colorBlack = Color.WHITE
                    it.colorWhite = Color.BLACK

                    it.bitmap.scale(300, 300, false)
                }
            }
        }

        if (content.isBlank()) {
            Image(
                modifier = modifier,
                imageVector = Icons.Default.QrCode,
                contentDescription = "blank",
            )
        } else {
            Image(
                modifier = modifier,
                bitmap = render().asImageBitmap(),
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
                            render(),
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
