package ru.gfastg98.qr_scanner_compose

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.gfastg98.qr_scanner_compose.ui.theme.QR_scanner_composeTheme

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            QR_scanner_composeTheme {
                val context = LocalContext.current
                Handler().postDelayed(
                    {
                        //context.startActivity(Intent(context, QRScannerActivity::class.java))
                        context.startActivity(Intent(context,MainActivity::class.java))
                        finish()
                    },3000)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Icon(imageVector = Icons.Sharp.Android, contentDescription = "Logo")
                }
            }
        }
    }
}