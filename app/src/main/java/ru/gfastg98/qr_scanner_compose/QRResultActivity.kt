package ru.gfastg98.qr_scanner_compose

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.common.Barcode.GeoPoint
import com.google.mlkit.vision.barcode.common.Barcode.UrlBookmark
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.gfastg98.qr_scanner_compose.domain.QRCodeResultViewModel
import ru.gfastg98.qr_scanner_compose.presentation.components.Screen
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

private val TAG = QRResultActivity::class.java.simpleName

class QRResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRScannerTheme {
                QRCodeViewer()
            }
        }
    }
}

@Composable
private fun QRCodeViewer() = Screen {
    val activity = LocalActivity.current
    val config = LocalWindowInfo.current.containerSize
    val context = LocalContext.current
    val intent = activity?.intent ?: return@Screen

    val vm = koinViewModel<QRCodeResultViewModel>(parameters = { parametersOf(intent) })
    val state by vm.state.collectAsStateWithLifecycle()

    val isForView = remember { !intent.getBooleanExtra("view", false) }

    title = "QR-код"

    actions {
        IconButton(onClick = {
            vm.shareAction(context, state.qrCodeEntity)
        }) {
            Icon(Icons.Default.Share, null)
        }
    }

    navigationIconAction {
        activity.finish()
    }

    content {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        ) {
            Image(
                bitmap = state.qrCodeEntity.bitmap.decodeToImageBitmap(),
                contentDescription = "mainImage",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(with(LocalDensity.current) { config.width.toDp() })
            )

            Box(
                Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SelectionContainer {
                    Text(
                        when (val info = state.barcodeInfo) {
                            is GeoPoint -> {
                                "Точка на карте\nКоординаты: ${info.lng}, ${info.lat}"
                            }

                            else -> state.qrCodeEntity.content
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(10),
                    onClick = {
                        vm.copyToClipboard(context)
                    }
                ) {
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
                        vm.copyToClipboardImage(context)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "QR код"
                    )
                }
            }

            if (isForView) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10),
                    onClick = {
                        vm.saveToDatabase()
                        (context as Activity).finish()
                    }
                ) {
                    Text("Сохранить в галерее и закрыть")
                }
            }

            if (state.barcodeInfo is UrlBookmark || state.barcodeInfo is GeoPoint) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10),
                    onClick = {
                        when (val info = state.barcodeInfo) {
                            is UrlBookmark -> {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW)
                                        .setData(info.url!!.toUri())
                                )
                            }

                            is GeoPoint -> {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW)
                                        .setData(
                                            "geo:${info.lat},${info.lng}?q=${info.lat},${info.lng}".toUri()
                                        )
                                )
                            }
                        }
                    }
                ) {
                    Text(
                        when (state.barcodeInfo) {
                            is UrlBookmark -> "Открыть ссылку"
                            else -> "Открыть карту" // GeoPoint
                        }
                    )
                }
            }
        }
    }
}

