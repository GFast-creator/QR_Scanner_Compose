package ru.gfastg98.qr_scanner_compose.presentation.generator

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.gfastg98.qr_scanner_compose.presentation.components.Keyboard
import ru.gfastg98.qr_scanner_compose.presentation.components.keyboardAsState
import ru.gfastg98.qr_scanner_compose.presentation.utils.showToast

private const val TAG = "QRCodeGeneratorScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun QRCodeGeneratorScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Blue)
            .padding(8.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val vm = koinViewModel<QRCodeGeneratorViewModel>()
        val context = LocalContext.current
        val isKeyboardOpen by keyboardAsState()
        val size by animateDpAsState(
            if (isKeyboardOpen == Keyboard.Opened) 200.dp
            else with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() - 20.dp },
            label = "qr code preview size animation"
        )
        var content by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(content) { vm.generate(content) }

        val bitmap by vm.generationResult.collectAsStateWithLifecycle()

        val modifier = Modifier
            .animateContentSize(tween(300))
            .size(size)
            .background(Color.White)

        if (bitmap == null) {
            Image(
                modifier = modifier,
                imageVector = Icons.Rounded.QrCode,
                contentDescription = "blank",
            )
        } else {
            Image(
                modifier = modifier,
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "image of generated qrcode"
            )
        }

        TextField(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .imePadding(),
            maxLines = 3,
            value = content,
            label = { Text(text = "Данные") },
            onValueChange = { content = it }
        )

        Button(
            modifier = Modifier,
            onClick = {
                if (bitmap != null) {
                    vm.viewFull(context, bitmap!!, content)
                } else context.showToast("Неверные данные")
            },
            enabled = content.isNotBlank()
        ) {
            Text("Сохранить")
        }
    }
}
