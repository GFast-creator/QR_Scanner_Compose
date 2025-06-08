package ru.gfastg98.qr_scanner_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.gfastg98.qr_scanner_compose.fragments.DBGenShowFragment
import ru.gfastg98.qr_scanner_compose.fragments.DBSaveShowFragment
import ru.gfastg98.qr_scanner_compose.fragments.QRCodeGeneratorFragment
import ru.gfastg98.qr_scanner_compose.fragments.QRCodeScannerPreview
import ru.gfastg98.qr_scanner_compose.ui.theme.QRScannerTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navItems = listOf(
            NavigationItem(
                getString(R.string.scanner),
                Icons.Filled.CameraAlt,
                Icons.Rounded.CameraAlt,
                "scanner"
            ),
            NavigationItem(
                getString(R.string.saved),
                Icons.Filled.Save,
                Icons.Rounded.Save,
                "db_scan"
            ),
            NavigationItem(
                getString(R.string.generator),
                Icons.Filled.QrCodeScanner,
                Icons.Rounded.QrCodeScanner,
                "generator"
            ),
            NavigationItem(
                getString(R.string.generated),
                Icons.Filled.DataSaverOff,
                Icons.Rounded.DataSaverOff,
                "db_gen"
            )
        )

        setContent {
            QRScannerTheme {
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    var selectedItemIndex by rememberSaveable {
                        mutableIntStateOf(0)
                    }

                    ModalNavigationDrawer(
                        modifier = Modifier.fillMaxSize(),
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                navItems.forEachIndexed { index, item ->
                                    NavigationDrawerItem(
                                        label = {
                                            Text(text = item.title)
                                        },
                                        selected = index == selectedItemIndex,
                                        onClick = {
                                            navController.navigate(item.route){
                                                /*popUpTo(navController.graph.findStartDestination().id){
                                                    saveState = true
                                                }*/
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
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(navItems[selectedItemIndex].title)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "menu"
                                            )
                                        }
                                    }
                                )
                            }
                        ) {innerPadding ->
                            NavHost(modifier = Modifier.padding(innerPadding), navController = navController, startDestination = "qr_code_scanner"){
                                navigation(
                                    startDestination = "scanner",
                                    route = "qr_code_scanner"
                                ){
                                    composable("scanner"){
                                        QRCodeScannerPreview()
                                    }
                                    composable("generator"){
                                        QRCodeGeneratorFragment()
                                    }
                                    composable("db_scan"){
                                        DBSaveShowFragment()
                                    }
                                    composable("db_gen"){
                                        DBGenShowFragment()
                                    }
                                }
                            }
                        }
                    }
                }
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