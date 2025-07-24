package ru.gfastg98.qr_scanner_compose.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import java.io.ByteArrayOutputStream
import kotlin.random.Random

@Composable
fun LazyStaggeredGridItemScope.QRCodeCard(
    item: QRCodeEntity,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(5.dp, 5.dp, 5.dp, 5.dp)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .border(
                4.dp,
                if (isSelected) MaterialTheme.colorScheme.error else Color.Transparent,
                CardDefaults.shape
            )
            .animateItem(),
        /*colors =
            if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)
            else CardDefaults.cardColors(),*/
    ) {
        Image(
            bitmap = item.bitmap.decodeToImageBitmap(),
            contentDescription = item.content,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(5.dp, 5.dp, 5.dp)
                .clip(CardDefaults.shape)
                .aspectRatio(1f)
                .align(CenterHorizontally),
        )
        Text(
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(5.dp),
            text = item.content,
            overflow = TextOverflow.Ellipsis,
            maxLines = 5,
        )
    }
}

@Preview
@Composable
private fun QRCodeCardPreview() {
    val bitmap = remember {
        val bitmap = createBitmap(300, 300).applyCanvas {
            drawRect(
                android.graphics.Rect(0, 0, width, height),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                }
            )
            drawCircle(
                150f,
                150f,
                150f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLUE
                }
            )
        }
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.toByteArray()
    }
    val mockQRCode = QRCodeEntity(
        uid = 0,
        bitmap = bitmap,
        content = "test",
        generated = true,
        barcodeObjectJson = "{}",
        codeFormat = 1,
    )
    val list = remember {
        listOf(
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
            mockQRCode.copy(content = "test".repeat(Random.nextInt(1, 30))),
        )
    }
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Fixed(3)
    ) {
        items(list) {
            QRCodeCard(it, true, {}, {})
        }
    }
}