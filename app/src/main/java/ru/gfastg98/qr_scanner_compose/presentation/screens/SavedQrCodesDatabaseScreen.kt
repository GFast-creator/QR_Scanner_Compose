package ru.gfastg98.qr_scanner_compose.presentation.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.presentation.components.QRCodeCard
import ru.gfastg98.qr_scanner_compose.presentation.components.ScreenScope
import ru.gfastg98.qr_scanner_compose.presentation.main.MainScreenViewModel

private const val TAG = "DBSaveShowFragment"

@Composable
fun ScreenScope.SavedQrCodesDatabaseScreen() {
    DatabaseTableScreen(generated = false)
}


@Composable
fun ScreenScope.DatabaseTableScreen(generated: Boolean = false) {
    val context = LocalContext.current
    val vm = koinViewModel<MainScreenViewModel>()
    val table by remember { vm.queryTable(generated) }.collectAsStateWithLifecycle()

    Column {
        if (table.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    modifier = Modifier.size(DpSize(200.dp, 200.dp)),
                    imageVector = Icons.Default.Terrain,
                    contentDescription = "no data to show",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
                Text("Нет сохраннённых QR-кодов")
            }
        } else {
            var selectedItems = remember { mutableStateListOf<Int>() }
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
                columns = StaggeredGridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
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
            }
        }
    }
}