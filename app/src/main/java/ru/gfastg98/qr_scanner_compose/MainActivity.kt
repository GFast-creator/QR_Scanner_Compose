package ru.gfastg98.qr_scanner_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DataSaverOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DataSaverOff
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.domain.utils.showToast
import ru.gfastg98.qr_scanner_compose.presentation.components.Screen
import ru.gfastg98.qr_scanner_compose.presentation.screens.GeneratedQrCodeDatabaseScreen
import ru.gfastg98.qr_scanner_compose.presentation.screens.QRCodeGeneratorScreen
import ru.gfastg98.qr_scanner_compose.presentation.screens.QRCodeScannerScreen
import ru.gfastg98.qr_scanner_compose.presentation.screens.SavedQrCodesDatabaseScreen
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRScannerTheme {
                MainScreen()
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
            "scanner"
        ),
        NavigationItem(
            stringResource(R.string.saved),
            Icons.Filled.Save,
            Icons.Rounded.Save,
            "db_scan"
        ),
        NavigationItem(
            stringResource(R.string.generator),
            Icons.Filled.QrCodeScanner,
            Icons.Rounded.QrCodeScanner,
            "generator"
        ),
        NavigationItem(
            stringResource(R.string.generated),
            Icons.Filled.DataSaverOff,
            Icons.Rounded.DataSaverOff,
            "db_gen"
        )
    )

@Composable
private fun MainScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        navController.enableOnBackPressed(false)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    var doubleTouch by remember { mutableStateOf(false) }

    Screen {
        navigationIcon {
            IconButton(onClick = {
                if (drawerState.isOpen)
                    scope.launch { drawerState.close() }
                else scope.launch { drawerState.open() }
            }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    null
                )
            }
        }
        content {
            val navItems = navigationItems
            LaunchedEffect(selectedItemIndex) {
                title = navItems[selectedItemIndex].title
            }
            ModalNavigationDrawer(
                modifier = Modifier.fillMaxSize(),
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Text(
                            stringResource(R.string.app_name),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        navigationItems.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                label = {
                                    Text(text = item.title)
                                },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }

                                    selectedItemIndex = index
                                    scope.launch { drawerState.close() }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedItemIndex == index)
                                            item.selectedItem else item.unselectedItem,
                                        contentDescription = item.title
                                    )
                                }
                            )
                        }
                    }
                }
            ) {
                NavHost(
                    modifier = Modifier,
                    navController = navController,
                    startDestination = "scanner"
                ) {
                    composable("scanner") {
                        QRCodeScannerScreen()
                    }
                    composable("generator") {
                        QRCodeGeneratorScreen()
                    }
                    composable("db_scan") {
                        SavedQrCodesDatabaseScreen()
                    }
                    composable("db_gen") {
                        GeneratedQrCodeDatabaseScreen()
                    }
                }
            }

            BackHandler {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (!doubleTouch) {
                    context.showToast("Нажмите ещё раз чтобы выйти...")
                    doubleTouch = true

                    scope.launch {
                        delay(1500)
                        doubleTouch = false
                    }
                } else activity?.finish()
            }
        }
    }
}

data class NavigationItem(
    var title: String,
    var selectedItem: ImageVector,
    var unselectedItem: ImageVector,
    val route: String
)