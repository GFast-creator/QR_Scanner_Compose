package ru.gfastg98.qr_scanner_compose

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.drawable.toDrawable
import ru.gfastg98.qr_scanner_compose.ui.theme.QR_scanner_composeTheme


class QRResultActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QR_scanner_composeTheme {
                // A surface container using the 'background' color from the theme
                TopAppBar(title = {
                    Text("QR Code Result")
                })
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    intent.getStringExtra("file_name")?.let {
                        val f = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        val b = BitmapFactory.decodeFile(f.toString() + "/QRCODES/$it")
                        Log.e("kilo", f.toString())
                        Image(
                            b.asImageBitmap(),
                            "mainImage"
                        )
                        
                    }
                }
            }
        }
    }
}