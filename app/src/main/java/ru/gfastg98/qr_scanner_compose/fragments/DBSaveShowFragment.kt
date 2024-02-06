package ru.gfastg98.qr_scanner_compose.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGSaver
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ru.gfastg98.qr_scanner_compose.DBHelper
import ru.gfastg98.qr_scanner_compose.QRResultActivity
import ru.gfastg98.qr_scanner_compose.query

@Composable
fun DBSaveShowFragment() {
    DBShow(generated = false)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DBShow(generated: Boolean = false) {
    val context = LocalContext.current
    val db = DBHelper(context).readableDatabase

    val query = {
        db.query(
            DBHelper.Contract.QRCodeEntry.TABLE_NAME,
            arrayOf("_id", "bitmap", "content","barcode_obj_js", "code_format"),
            "generated = ${generated.toString().uppercase()}"
        )
    }
    var cursor = query()

    Column {
        if (cursor.count < 1){

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    modifier = Modifier.size(DpSize(200.dp, 200.dp)),
                    imageVector = Icons.Default.Terrain,
                    contentDescription = "no data to show"
                )
                Text("Нет сохраннённых QR-кодов")
            }
        } else {
            cursor.moveToFirst()
            var selectMode by remember {
                mutableStateOf(false)
            }
            var selectedItems by remember {
                mutableStateOf(emptyList<Int>())
            }

            Row{
                Button(onClick = {
                    selectMode = !selectMode
                    selectedItems = emptyList()
                },
                    colors = if (selectMode) ButtonDefaults.buttonColors(containerColor = Color.Red)
                    else ButtonDefaults.buttonColors()
                ) {
                    Icon(imageVector = Icons.Default.PhotoSizeSelectLarge,
                        contentDescription = "Режим выбора элементов")
                }
                if(selectMode)
                    Button(
                        onClick = {
                            val list = mutableListOf<String>()
                            for (selectedItem in selectedItems) {
                                with(cursor){
                                    moveToPosition(selectedItem)
                                    list += getInt(0).toString()
                                }
                            }
                            db.delete(
                                DBHelper.Contract.QRCodeEntry.TABLE_NAME,
                                with("_id IN (${"?, ".repeat(selectedItems.size)}"){
                                    removeRange(lastIndex-1..lastIndex) + ")"
                                }, list.toTypedArray())
                            cursor = query()
                            selectedItems = mutableListOf()
                            selectMode = false
                        },
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить")
                    }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
            ){
                var k = 0
                items(cursor.count, key = {k++}){id ->

                    val blob = cursor.getBlob(1)
                    val content = cursor.getString(2)
                    val barcode_obj = cursor.getString(3)
                    val code_format = cursor.getInt(4)

                    Card(
                        modifier = Modifier
                            .padding(5.dp, 0.dp, 0.dp, 5.dp)
                            .clickable {
                                if (selectMode) {
                                    if (selectedItems.contains(id))
                                        selectedItems -= id
                                    else selectedItems += id
                                    Log.i("kilo", selectedItems.joinToString(", "))
                                } else {
                                    if (QRGSaver().save(
                                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                                            .path+"/QRCODES/",
                                        "intent",
                                        BitmapFactory.decodeByteArray(blob,0,blob.size),
                                        QRGContents.ImageType.IMAGE_PNG
                                    )){
                                        context.startActivity(
                                            Intent(
                                                context,
                                                QRResultActivity::class.java
                                            )
                                                .putExtra("file_name", "intent.png")
                                                .putExtra("content", content)
                                                .putExtra("view", true)
                                                .putExtra("barcode_obj", barcode_obj)
                                                .putExtra("code_format", code_format)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }

                            }
                            .animateItemPlacement(),
                        colors = if (selectedItems.contains(id))
                            CardDefaults.cardColors(containerColor = Color.Red)
                        else CardDefaults.cardColors(),

                    ) {
                        Image(
                            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
                                .asImageBitmap(),
                            contentDescription = content,
                            modifier = Modifier
                                .padding(5.dp)
                                .clip(RoundedCornerShape(20f))
                                .align(CenterHorizontally)
                        )
                        Text(content, Modifier.align(CenterHorizontally))
                    }
                    cursor.moveToNext()
                }
            }
        }
    }
}
