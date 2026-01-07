package ru.gfastg98.qr_scanner_compose.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.R
import ru.gfastg98.qr_scanner_compose.presentation.ObserveAsEvents
import ru.gfastg98.qr_scanner_compose.presentation.components.LocalNavigationState
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeCard
import ru.gfastg98.qr_scanner_compose.presentation.components.ScreenScope

private const val TAG = "MainScreen"

@Composable
fun ScreenScope.MainScreen() {
    val navigator = LocalNavigationState.current
    val viewModel = koinViewModel<MainScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            MainScreenEvent.NavigateToGenerator -> navigator.navigate(Route.Generator)
            MainScreenEvent.NavigateToScanner -> navigator.navigate(Route.Scanner)
        }
    }

    MainScreenRoot(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScreenScope.MainScreenRoot(
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = expanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = expanded,
                        onCheckedChange = { expanded = it }
                    ) {
                        Icon(Icons.Default.Edit, null)
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    text = { Text(stringResource(R.string.generator)) },
                    icon = { Icon(Icons.Default.AddHome, null) },
                    onClick = { onAction(MainScreenAction.ToGenerator) }
                )
                FloatingActionButtonMenuItem(
                    text = { Text(stringResource(R.string.scanner)) },
                    icon = { Icon(Icons.Default.CameraAlt, null) },
                    onClick = { onAction(MainScreenAction.ToScanner) }
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddings ->
        Column(Modifier.padding(paddings)) {
            if (state.table.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        modifier = Modifier.size(200.dp, 200.dp),
                        imageVector = Icons.Default.Terrain,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                    )
                    Text(stringResource(R.string.MainScreen__no_qr_codes))
                }
            } else {
                BackHandler(state.selected.isNotEmpty()) { onAction(MainScreenAction.UnselectAll) }
                LaunchedEffect(state.selected) {
                    actions {
                        if (state.selected.isNotEmpty()) {
                            IconButton(
                                onClick = { onAction(MainScreenAction.DeleteSelected) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить"
                                )
                            }
                        }
                    }
                }

                LazyVerticalStaggeredGrid(
                    modifier = Modifier.weight(1f),
                    verticalItemSpacing = 4.dp,
                    columns = StaggeredGridCells.Fixed(3)
                ) {
                    items(state.table, key = { it.uid }) { item ->
                        QRCodeCard(
                            item = item,
                            isSelected = state.selected == item,
                            onLongClick = {
                                if (state.selected.isEmpty()) {
                                    onAction(MainScreenAction.Select(item))
                                }
                            },
                            onClick = {
                                if (state.selected.isNotEmpty()) {
                                    if (state.selected.contains(item))
                                        onAction(MainScreenAction.Unselect(item))
                                    else onAction(MainScreenAction.Select(item))
                                } else {
                                    onAction(MainScreenAction.Open(item))
                                }
                            }
                        )
                    }
                    item {
                        Box(Modifier.size(FloatingActionButtonDefaults.LargeIconSize))
                    }
                }
            }
        }
        AnimatedVisibility(
            expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .alpha(0.7f)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun FabRow(
    text: String,
    icon: ImageVector,
    offset: (Int) -> Int = { it },
    visible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = offset),
        exit = fadeOut() + slideOutVertically(targetOffsetY = offset)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                color = Color.White,
                text = text
            )
            SmallFloatingActionButton(
                onClick = onClick
            ) {
                Icon(imageVector = icon, null)
            }
        }
    }
}