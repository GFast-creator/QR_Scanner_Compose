package ru.gfastg98.qr_scanner_compose

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme
import kotlin.time.Duration.Companion.seconds

// FIXME : Требуеться большая переработка
class StartActivity : ComponentActivity() {
    private val TAG = StartActivity::class.java.simpleName

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i(TAG, "Permission granted")
            lifecycleScope.launch {
                delay(3.seconds)
                navigateToMainActivity()
            }
        } else {
            Log.i(TAG, "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { StartScreen() }
    }

    @Composable
    private fun StartScreen() {
        QRScannerTheme {
            var openAlertDialog by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Icon(imageVector = Icons.Sharp.Android, contentDescription = "Logo")
            }

            if (!checkCameraPermission()) {
                if (openAlertDialog) {
                    AlertDialog(
                        icon = {
                            Icon(Icons.Default.QuestionMark, null)
                        },
                        title = {
                            Text(text = stringResource(R.string.StartActivity__dialog_title))
                        },
                        text = {
                            Text(stringResource(R.string.StartActivity__dialog_substring))
                        },
                        onDismissRequest = {
                            openAlertDialog = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    requestCameraPermission()
                                    openAlertDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.StartActivity_continue))
                            }
                        },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    )
                }
            } else {
                LaunchedEffect(Unit) {
                    navigateToMainActivity()
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

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}