package ru.gfastg98.qr_scanner_compose.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DataSaverOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DataSaverOff
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import ru.gfastg98.qr_scanner_compose.R
import ru.gfastg98.qr_scanner_compose.presentation.components.LocalNavigationState
import ru.gfastg98.qr_scanner_compose.presentation.components.Screen
import ru.gfastg98.qr_scanner_compose.presentation.generator.QRCodeGeneratorScreen
import ru.gfastg98.qr_scanner_compose.presentation.scanner.QrCodeScannerScreen
import ru.gfastg98.qr_scanner_compose.presentation.screens.GeneratedQrCodeDatabaseScreen
import ru.gfastg98.qr_scanner_compose.presentation.screens.SavedQrCodesDatabaseScreen
import ru.gfastg98.qr_scanner_compose.presentation.utils.showToast
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRScannerTheme {
                MainActivityScreen()
            }
        }
    }
}

@Stable
private val navigationItems
    @Composable get() = listOf(
        NavigationItem(
            stringResource(R.string.scanner),
            Icons.Filled.CameraAlt,
            Icons.Rounded.CameraAlt,
            Route.Scanner
        ),
        NavigationItem(
            stringResource(R.string.saved),
            Icons.Filled.Save,
            Icons.Rounded.Save,
            Route.DatabaseScanned
        ),
        NavigationItem(
            stringResource(R.string.generator),
            Icons.Filled.QrCodeScanner,
            Icons.Rounded.QrCodeScanner,
            Route.Generator
        ),
        NavigationItem(
            stringResource(R.string.generated),
            Icons.Filled.DataSaverOff,
            Icons.Rounded.DataSaverOff,
            Route.DatabaseGenerated
        )
    )

@Composable
private fun MainActivityScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val navController = rememberNavController()
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    var doubleTouch by remember { mutableStateOf(false) }
    LaunchedEffect(doubleTouch) {
        if (doubleTouch) {
            delay(1500)
            doubleTouch = false
        }
    }

    Screen {
        content {
            val navItems = navigationItems
            LaunchedEffect(selectedItemIndex) {
                title = navItems[selectedItemIndex].title
            }

            BackHandler {
                if (!doubleTouch) {
                    context.showToast(R.string.tap_again_to_exit)
                    doubleTouch = true
                } else {
                    activity?.finish()
                }
            }

            CompositionLocalProvider(LocalNavigationState provides navController) {
                NavHost(
                    modifier = Modifier,
                    navController = navController,
                    startDestination = Route.MainScreen
                ) {
                    composable(Route.MainScreen::class) { MainScreen() }
                    composable(Route.Scanner::class) { QrCodeScannerScreen() }
                    composable(Route.Generator::class) { QRCodeGeneratorScreen() }
                    composable(Route.DatabaseGenerated::class) { GeneratedQrCodeDatabaseScreen() }

                    // noop
                    composable(Route.DatabaseScanned::class) { SavedQrCodesDatabaseScreen() }
                }
            }
        }
    }
}

@Serializable
sealed interface Route {
    @Serializable
    data object MainScreen : Route

    @Serializable
    data object Scanner : Route

    @Serializable
    data object Generator : Route

    @Serializable
    data object DatabaseScanned : Route

    @Serializable
    data object DatabaseGenerated : Route
}

data class NavigationItem(
    var title: String,
    var selectedItem: ImageVector,
    var unselectedItem: ImageVector,
    val route: Route,
)

