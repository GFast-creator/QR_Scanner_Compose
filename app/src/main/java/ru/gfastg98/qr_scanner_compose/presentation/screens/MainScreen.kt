package ru.gfastg98.qr_scanner_compose.presentation.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.Route
import ru.gfastg98.qr_scanner_compose.domain.QRCodeDatabaseViewModel
import ru.gfastg98.qr_scanner_compose.presentation.components.LocalNavigationState
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeCard
import ru.gfastg98.qr_scanner_compose.presentation.components.ScreenScope

private const val TAG = "MainScreen"

@Composable
fun ScreenScope.MainScreen() {
    val navigator = LocalNavigationState.current
    val context = LocalContext.current

    val vm = koinViewModel<QRCodeDatabaseViewModel>()
    val table by vm.table.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            BackHandler(visible) { visible = false }
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 2 * it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 2 * it })
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Сгенерировать"
                        )
                        SmallFloatingActionButton(
                            onClick = { navigator.navigate(Route.Generator) }
                        ) {
                            Icon(imageVector = Icons.Default.BorderColor, null)
                        }
                    }
                }

                AnimatedVisibility(
                    visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Скарнировать"
                        )
                        SmallFloatingActionButton(
                            onClick = { navigator.navigate(Route.Scanner) }
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, null)
                        }
                    }
                }
                val animatedDpShape by animateIntAsState(
                    if (!visible) 20 else 100
                )
                FloatingActionButton(
                    shape = RoundedCornerShape(animatedDpShape),
                    onClick = { visible = !visible }
                ) {
                    Row {
                        //AnimatedVisibility(!visible) { Text(text = "Добавить") }
                        Icon(imageVector = Icons.Default.Add, null)
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { _ ->
        Column(Modifier.padding()) {
            if (table.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        modifier = Modifier.size(200.dp, 200.dp),
                        imageVector = Icons.Default.Terrain,
                        contentDescription = "no data to show",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                    )
                    Text("Нет сохраннённых QR-кодов")
                }
            } else {
                var selectedItems = remember { mutableStateListOf<Int>() }
                BackHandler(selectedItems.isNotEmpty()) { selectedItems.clear() }
                LaunchedEffect(selectedItems) {
                    actions {
                        if (selectedItems.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    vm.deleteAll(
                                        selectedItems.mapNotNull { id ->
                                            table.find { i ->
                                                i.uid == id
                                            }
                                        }
                                    )
                                    selectedItems.clear()
                                }
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
                    items(table, key = { it.uid }) { item ->
                        QRCodeCard(
                            item = item,
                            isSelected = selectedItems.contains(item.uid),
                            onLongClick = {
                                if (selectedItems.isEmpty()) {
                                    selectedItems += item.uid
                                }
                            },
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    if (selectedItems.contains(item.uid))
                                        selectedItems -= item.uid
                                    else selectedItems += item.uid
                                    Log.i(TAG, selectedItems.joinToString(", "))
                                } else {
                                    vm.fullView(context, item)
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
            visible,
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