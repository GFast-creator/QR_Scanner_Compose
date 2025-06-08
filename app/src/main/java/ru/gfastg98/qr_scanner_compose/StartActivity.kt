package ru.gfastg98.qr_scanner_compose

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.sharp.Android
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.ui.theme.QR_scanner_composeTheme

class StartActivity : ComponentActivity() {
    companion object {
        val TAG = StartActivity::class.java.simpleName
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i(TAG, "Permission granted")
            Handler().postDelayed(
                {
                    startActivity(
                        Intent(
                            this@StartActivity,
                            MainActivity::class.java
                        )
                    )
                }, 3000
            )
        } else {
            Log.i(TAG, "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            setContent {
                QR_scanner_composeTheme {
                    val openAlertDialog = remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Icon(imageVector = Icons.Sharp.Android, contentDescription = "Logo")
                    }
                    if (checkCameraPermission())
                        if (openAlertDialog.value) {
                            AlertDialog(
                                icon = {
                                    Icon(
                                        Icons.Default.QuestionMark,
                                        contentDescription = "Example Icon"
                                    )
                                },
                                title = {
                                    Text(text = "Разрешения")
                                },
                                text = {
                                    Text(
                                        text = "Для продолжения нужны выдать все следующие резрешения.\nЕсли вы самостоятельно" +
                                                "отключали камеру, включите разрешение самостоятельно через настройки."
                                    )
                                },
                                onDismissRequest = {
                                    openAlertDialog.value = false
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            requestCameraPermission()
                                            openAlertDialog.value = false
                                        }
                                    ) {
                                        Text("Продолжить")
                                    }
                                },
                                properties = DialogProperties(
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                )
                            )
                        }
                }
            }

        }
    }

    private fun checkCameraPermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        when {
            checkCameraPermission() -> {
                Log.i(TAG, "Permission previously granted")
                return
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> Log.i(TAG, "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
}