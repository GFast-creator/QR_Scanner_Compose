package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import ru.gfastg98.qr_scanner_compose.R

typealias ScreenActions = @Composable RowScope.() -> Unit
typealias ScreenBottomBar = @Composable () -> Unit
typealias ScreenContent = @Composable ColumnScope.(ScreenScope) -> Unit
typealias ScreenFab = @Composable () -> Unit
typealias ScreenNavigationIcon = @Composable () -> Unit
typealias ScreenBackAction = (NavController) -> Unit
typealias LeadScreen = @Composable BoxScope.() -> Unit

class ScreenScope {
    var title by mutableStateOf<String?>(null)
    var navigationIconVisibility: Boolean by mutableStateOf(true)
    var scrollable: Boolean by mutableStateOf(false)
    private var leadScreen: LeadScreen by mutableStateOf({})

    private var actions: ScreenActions by mutableStateOf(@Composable {})
    private var bottomBar: ScreenBottomBar by mutableStateOf(@Composable {})
    private var fab: ScreenFab by mutableStateOf(@Composable {})

    private var navigationIconAction: ScreenBackAction by mutableStateOf({
        it.navigateUp()
    })

    private var navigationIcon: ScreenNavigationIcon by mutableStateOf(@Composable {
        /* val navController = LocalNavigationState.current

         IconButton(
             onClick = {
                 navigationIconAction(navController)
             }
         ) {
             Image(
                 imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                 contentDescription = null
             )
         }*/
    })

    private var content: ScreenContent = @Composable {}

    fun actions(actions: ScreenActions) {
        this.actions = actions
    }

    fun bottomBar(bottomBar: ScreenBottomBar) {
        this.bottomBar = bottomBar
    }

    fun content(content: ScreenContent) {
        this.content = content
    }

    fun floatingActionButton(fab: ScreenFab) {
        this.fab = fab
    }

    fun navigationIcon(navigationIcon: ScreenNavigationIcon) {
        this.navigationIcon = navigationIcon
    }

    fun navigationIconAction(navigationIconAction: ScreenBackAction) {
        this.navigationIconAction = navigationIconAction
    }

    fun leadScreen(leadScreen: LeadScreen) {
        this.leadScreen = leadScreen
    }

    fun clear() {
        fab = {}
        bottomBar = {}
        actions = {}
        leadScreen = {}
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Render() {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                ),
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        modifier = Modifier,
                        title = {
                            Text(
                                text = title ?: stringResource(id = R.string.app_name),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            navigationIcon.takeIf { navigationIconVisibility }?.invoke()
                        },
                        actions = { actions.invoke(this) }
                    )
                },
                bottomBar = bottomBar,
                floatingActionButton = fab,
                floatingActionButtonPosition = FabPosition.End,
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .run {
                            if (scrollable) verticalScroll(state = rememberScrollState())
                            else this
                        }
                        .padding(innerPadding)
                        .navigationBarsPadding()
                        .fillMaxWidth(),
                ) {
                    content(this@ScreenScope)
                }
            }
            leadScreen()
        }
    }
}