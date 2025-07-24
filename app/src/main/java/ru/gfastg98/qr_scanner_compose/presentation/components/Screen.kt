package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.gfastg98.qr_scanner_compose.R

val LocalNavigationState = staticCompositionLocalOf<NavController> {
    error("LocalNavController not initialized")
}

@Composable
fun Screen(builder: @Composable ScreenScope.() -> Unit) {
    val scope = remember { ScreenScope() }
    scope.builder()
    scope.Render()
}

@Preview(
    showBackground = true
)
@Composable
fun ScreenComponentPreview() = Screen {
    title = stringResource(id = R.string.app_name)

    navigationIcon {}

    actions {
        IconButton(
            onClick = { /*...*/ }
        ) {
            Image(imageVector = Icons.Default.Home, contentDescription = null)
        }
    }

    content { screenScope ->
        Box(
            Modifier
                .size(100.dp)
                .background(Color.Gray)
        ) {
            Button(
                onClick = {
                    screenScope.actions {
                        Text("123")
                    }
                }
            ) {
                Text("test")
            }
        }
    }
}
