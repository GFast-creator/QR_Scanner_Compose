package ru.gfastg98.qr_scanner_compose.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import ru.gfastg98.qr_scanner_compose.presentation.components.tip.tooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeTypeIcon(
    modifier: Modifier = Modifier,
    type: Int
) {
    val icon = remember { getIconByType(type) } ?: return
    Surface(
        modifier = modifier.tooltip("Тип QR - кода"),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = icon,
            contentDescription = "QR - code type"
        )
    }
}

fun getIconByType(type: Int): ImageVector? =
    when (type) {
        Barcode.TYPE_GEO -> Icons.Default.LocationOn
        Barcode.TYPE_SMS -> Icons.Default.Sms
        Barcode.TYPE_URL -> Icons.Default.Link
        Barcode.TYPE_EMAIL -> Icons.Default.Email
        Barcode.TYPE_CALENDAR_EVENT -> Icons.Default.CalendarToday
        Barcode.TYPE_CONTACT_INFO -> Icons.Default.Contacts
        Barcode.TYPE_PHONE -> Icons.Default.Phone
        Barcode.TYPE_PRODUCT -> Icons.Default.AddBusiness
        Barcode.TYPE_TEXT -> Icons.Default.TextFields
        Barcode.TYPE_WIFI -> Icons.Default.Wifi
        Barcode.TYPE_DRIVER_LICENSE -> Icons.Default.Tablet
        else -> null // no-op
    }