package ru.gfastg98.qr_scanner_compose.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import ru.gfastg98.qr_scanner_compose.R
import ru.gfastg98.qr_scanner_compose.presentation.main.MainActivity
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme
import kotlin.time.Duration.Companion.seconds

class StartActivity : ComponentActivity() {

    private val permissionsToRequest: Array<String>
        get() {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            return permissions.toTypedArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StartScreen() }
    }

    @Composable
    private fun StartScreen() {
        QRScannerTheme {
            var permissionsGranted by remember { mutableStateOf(checkAllPermissionsGranted()) }
            var showDialog by remember { mutableStateOf(!permissionsGranted) }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                permissionsGranted = allGranted
                if (!allGranted) {
                    showDialog = true
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Icon(imageVector = Icons.Sharp.Android, contentDescription = "Logo")
            }

            if (permissionsGranted) {
                LaunchedEffect(Unit) {
                    delay(1.seconds)
                    navigateToMainActivity()
                }
            } else {
                if (showDialog) {
                    AlertDialog(
                        icon = {
                            Icon(Icons.Default.QuestionMark, null)
                        },
                        title = {
                            Text(stringResource(R.string.StartActivity__dialog_title))
                        },
                        text = {
                            Text(stringResource(R.string.StartActivity__dialog_substring))
                        },
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    launcher.launch(permissionsToRequest)
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
            }
        }
    }

    private fun checkAllPermissionsGranted(): Boolean {
        return permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
