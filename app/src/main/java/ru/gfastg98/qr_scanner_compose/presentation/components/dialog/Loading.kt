package ru.gfastg98.qr_scanner_compose.presentation.components.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import ru.gfastg98.qr_scanner_compose.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingDialog(
    onDismissRequest: () -> Unit = {},
    @StringRes textId: Int = R.string.LoadingDialog__loading,
) {
    val text = stringResource(textId)
    LoadingDialog(onDismissRequest, text)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingDialog(
    onDismissRequest: () -> Unit = {},
    text: String,
) {
    Dialog(onDismissRequest) {
        Column {
            LoadingIndicator()
            Text(text)
        }
    }
}