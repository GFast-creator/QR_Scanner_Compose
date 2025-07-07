package ru.gfastg98.qr_scanner_compose.presentation.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.domain.QRCodeDatabaseViewModel
import ru.gfastg98.qr_scanner_compose.presentation.components.ScreenScope

private const val TAG = "DBSaveShowFragment"

@Composable
fun ScreenScope.SavedQrCodesDatabaseScreen() {
    DatabaseTableScreen(generated = false)
}


@Composable
fun ScreenScope.DatabaseTableScreen(generated: Boolean = false) {
    val context = LocalContext.current
    val vm = koinViewModel<QRCodeDatabaseViewModel>()
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
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

@Composable
private fun LazyGridItemScope.QRCodeCard(
    item: QRCodeEntity,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(5.dp, 0.dp, 0.dp, 5.dp)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .animateItem(),
        colors = if (isSelected) CardDefaults.cardColors(containerColor = Color.Red)
        else CardDefaults.cardColors(),
    ) {
        Image(
            bitmap = item.bitmap.decodeToImageBitmap(),
            contentDescription = item.content,
            modifier = Modifier
                .padding(5.dp)
                .clip(RoundedCornerShape(20f))
                .align(CenterHorizontally)
        )
        Text(item.content, Modifier.align(CenterHorizontally))
    }
}