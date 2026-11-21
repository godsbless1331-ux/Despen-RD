package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MonetizationConstants
import com.example.data.model.Category
import com.example.data.model.Location
import com.example.data.model.PantryItem
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeExpired
import com.example.ui.theme.BadgeExpiringSoon
import com.example.ui.theme.BadgeReplenish
import com.example.ui.viewmodel.LimitType
import com.example.ui.viewmodel.PantryViewModel

@Composable
fun PantryScreen(
    viewModel: PantryViewModel,
    onEditItem: (PantryItem) -> Unit,
    onOpenManualAdd: () -> Unit,
    onOpenCaptureSheet: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.filteredPantryItems.collectAsState()
    val allItems by viewModel.rawPantryItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val config by viewModel.config.collectAsState()

    var wasteItem by remember { mutableStateOf<PantryItem?>(null) }
    var wasteReason by remember { mutableStateOf("Vencimiento") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Inventario del Hogar",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (config.isProUser) {
                            "${allItems.size} productos (Ilimitado Pro)"
                        } else {
                            "${allItems.size}/${MonetizationConstants.FREE_PRODUCT_LIMIT} productos (Plan Gratis)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!config.isProUser && allItems.size >= 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                PermanentProBadge(
                    isProUser = config.isProUser,
                    onOpenPaywall = onOpenPaywall
                )
            }
        }

        // Direct Manual Upload & Multi-Capture Quick Actions (keeps all existing capture options)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenManualAdd,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pantry_manual_add_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir manual", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onOpenCaptureSheet,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pantry_capture_options_button")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Factura / Scan", fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }

        // Limit banner if user reaches 30 in Free
        if (!config.isProUser && allItems.size >= 30) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF0F0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPaywall() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Límite de 30 productos alcanzado",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Toca para desbloquear Despensa Pro y añadir sin límites.",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar en el inventario...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pantry_search_input"),
                singleLine = true
            )
        }

        // Location Filter Tabs (Todas, Despensa, Nevera, Congelador)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedLocation == "all",
                    onClick = { viewModel.setLocationFilter("all") },
                    label = { Text("Todas las ubicaciones") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Location.entries.forEach { loc ->
                    FilterChip(
                        selected = selectedLocation == loc.id,
                        onClick = { viewModel.setLocationFilter(loc.id) },
                        label = { Text("${loc.emoji} ${loc.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == "all",
                    onClick = { viewModel.setCategoryFilter("all") },
                    label = { Text("Todos") }
                )
                Category.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.id,
                        onClick = { viewModel.setCategoryFilter(cat.id) },
                        label = { Text("${cat.emoji} ${cat.displayName}") }
                    )
                }
            }
        }

        // Items List
        if (items.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 32.sp)
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "all" || selectedLocation != "all") "No se encontraron productos" else "Tu inventario está vacío",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sube productos manualmente o usa el botón (+) para facturas, códigos de barras o dictado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenManualAdd,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Subir manual")
                            }
                            OutlinedButton(
                                onClick = onOpenCaptureSheet,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Otras opciones")
                            }
                        }
                    }
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                PantryItemCard(
                    item = item,
                    onEdit = { onEditItem(item) },
                    onIncrement = {
                        viewModel.saveItem(item.copy(qty = item.qty + 1.0))
                    },
                    onDecrement = {
                        viewModel.decrementQuantity(item.id, "Consumo")
                    },
                    onRecordWaste = {
                        wasteItem = item
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Waste / Spoilage Modal Dialog
    wasteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { wasteItem = null },
            title = { Text("Registrar Merma / Desperdicio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "¿Deseas registrar la pérdida de ${item.name} (${item.qty} ${item.unit})?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Esto restará el producto de la despensa y registrará la pérdida en el libro contable del hogar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Vencimiento", "Dañado", "Cocina/Mala preparación").forEach { reason ->
                            FilterChip(
                                selected = wasteReason == reason,
                                onClick = { wasteReason = reason },
                                label = { Text(reason, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordWaste(item.id, item.qty, wasteReason)
                        wasteItem = null
                        Toast.makeText(context, "Merma registrada en el historial", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar merma")
                }
            },
            dismissButton = {
                TextButton(onClick = { wasteItem = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PantryItemCard(
    item: PantryItem,
    onEdit: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRecordWaste: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pantry_item_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = item.cat.emoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Location pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${item.location.emoji} ${item.location.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (item.brand.isNotBlank()) {
                                Text(
                                    text = "· ${item.brand}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // Expiry status indicator
                val daysUntil = item.daysUntilExpiry()
                if (daysUntil != null) {
                    val (badgeColor, label) = when {
                        daysUntil < 0 -> Pair(BadgeExpired, "Vencido")
                        daysUntil == 0L -> Pair(BadgeExpired, "Vence hoy")
                        daysUntil <= 3 -> Pair(BadgeExpiringSoon, "Vence en $daysUntil d")
                        daysUntil <= 7 -> Pair(BadgeReplenish, "$daysUntil días")
                        else -> Pair(MaterialTheme.colorScheme.outline, "Exp: ${item.expDate}")
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = label,
                            color = badgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Controls & Quantity row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onDecrement() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(
                        text = "${item.qty} ${item.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onIncrement() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Más", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onRecordWaste) {
                        Text("Merma", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
