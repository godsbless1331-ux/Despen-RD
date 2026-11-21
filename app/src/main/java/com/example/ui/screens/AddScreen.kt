package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MonetizationConstants
import com.example.ui.components.PermanentProBadge
import com.example.ui.viewmodel.LimitType
import com.example.ui.viewmodel.PantryViewModel

@Composable
fun AddScreen(
    viewModel: PantryViewModel,
    onOpenScan: () -> Unit,
    onOpenPaste: () -> Unit,
    onOpenManual: () -> Unit,
    onOpenReceiptPhoto: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    val allItems by viewModel.rawPantryItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Añadir productos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (config.isProUser) "Acceso Pro Ilimitado" else "Plan Gratis",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            PermanentProBadge(
                isProUser = config.isProUser,
                onOpenPaywall = onOpenPaywall
            )
        }

        // Limits status bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Capacidad de despensa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (config.isProUser) "${allItems.size} productos (Ilimitado)"
                        else "${allItems.size}/${MonetizationConstants.FREE_PRODUCT_LIMIT} productos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!config.isProUser && allItems.size >= 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Escaneos este mes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (config.isProUser) "Ilimitado (Pro)"
                        else "${config.monthlyReceiptScansCount}/${MonetizationConstants.FREE_MONTHLY_RECEIPT_SCANS_LIMIT} escaneos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!config.isProUser && config.monthlyReceiptScansCount >= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2x2 Grid of creation methods
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddMethodCard(
                icon = Icons.Default.QrCodeScanner,
                emoji = "▦",
                title = "Escanear código",
                subtitle = "Código de barras con cámara",
                testTag = "add_method_scan",
                onClick = {
                    if (viewModel.canAddProduct(false)) {
                        onOpenScan()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            AddMethodCard(
                icon = Icons.Default.CameraAlt,
                emoji = "🧾",
                title = "Foto de factura",
                subtitle = if (config.isProUser) "Ilimitado (Pro)" else "${config.monthlyReceiptScansCount}/5 por mes",
                testTag = "add_method_photo",
                onClick = {
                    if (!viewModel.canScanReceipt()) {
                        viewModel.showLimitDialog(LimitType.RECEIPT_SCAN_LIMIT)
                    } else if (viewModel.canAddProduct(false)) {
                        onOpenReceiptPhoto()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddMethodCard(
                icon = Icons.Default.ContentPaste,
                emoji = "📋",
                title = "Pegar factura o lista",
                subtitle = "Una línea por producto",
                testTag = "add_method_paste",
                onClick = {
                    if (viewModel.canAddProduct(false)) {
                        onOpenPaste()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            AddMethodCard(
                icon = Icons.Default.Edit,
                emoji = "✍️",
                title = "Manual",
                subtitle = "Cualquier producto",
                testTag = "add_method_manual",
                onClick = {
                    if (viewModel.canAddProduct(false)) {
                        onOpenManual()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Info Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "💡 Consejo para tu despensa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Puedes pegar listas completas de WhatsApp o de tu bloc de notas con «Pegar factura o lista». La app detecta automáticamente cantidades como «5 lb», «1 litro», o «3 paquetes» y clasifica en Alimentos, Limpieza o Bebé.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddMethodCard(
    icon: ImageVector,
    emoji: String,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
