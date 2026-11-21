package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShoppingListItemEntity
import com.example.data.model.PantryItem
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeOk
import com.example.ui.theme.BadgeReplenish
import com.example.ui.viewmodel.PantryViewModel

@Composable
fun ShoppingListScreen(
    viewModel: PantryViewModel,
    onMoveReminderToPantry: ((PantryItem, String) -> Unit)? = null,
    onOpenPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val lowStockItems = viewModel.rawPantryItems.collectAsState().value.filter { it.isLowStock }
    val config by viewModel.config.collectAsState()

    var newItemText by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemUnit by remember { mutableStateOf("ud") }
    var newItemEstPrice by remember { mutableStateOf("") }

    val checkedCount = shoppingItems.count { it.isChecked }
    val totalEstBudget = shoppingItems.sumOf { it.estPrice }

    fun sendToWhatsApp() {
        val text = viewModel.generateShoppingListText()
        if (text.isBlank()) {
            Toast.makeText(context, "La lista de compras está vacía", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = Uri.parse("https://wa.me/?text=${Uri.encode(text)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (_: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Enviar lista de compras"))
        }
    }

    fun copyToClipboard() {
        val text = viewModel.generateShoppingListText()
        if (text.isBlank()) {
            Toast.makeText(context, "La lista de compras está vacía", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Lista de compras", text))
        Toast.makeText(context, "Lista copiada al portapapeles", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MANAGEMENT TECH GEO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Lista Unificada",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                PermanentProBadge(
                    isProUser = config.isProUser,
                    onOpenPaywall = onOpenPaywall
                )
            }
        }

        // Budget & Actions Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Presupuesto Estimado",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "RD$ ${String.format(java.util.Locale.US, "%,.2f", totalEstBudget)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${shoppingItems.size} artículos",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { sendToWhatsApp() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedButton(
                            onClick = { copyToClipboard() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Checkout Button if any checked items
                    if (checkedCount > 0) {
                        Button(
                            onClick = {
                                val count = viewModel.checkoutShoppingList("Colmado Dominicano")
                                Toast.makeText(
                                    context,
                                    "¡Compra finalizada! $count productos ingresados a tu despensa.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BadgeOk),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finalizar compra ($checkedCount comprados)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Add to List Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Añadir a la lista",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("Producto o artículo") },
                        placeholder = { Text("Ej. Aceite Crisol, Leche, Plátanos...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            label = { Text("Cant.") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = newItemEstPrice,
                            onValueChange = { newItemEstPrice = it },
                            label = { Text("Precio RD$") },
                            placeholder = { Text("Opcional") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )

                        Button(
                            onClick = {
                                if (newItemText.isNotBlank()) {
                                    val qty = newItemQty.replace(',', '.').toDoubleOrNull() ?: 1.0
                                    val price = newItemEstPrice.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    viewModel.addShoppingItem(
                                        name = newItemText.trim(),
                                        qty = qty,
                                        unit = newItemUnit,
                                        estPrice = price,
                                        source = "MANUAL"
                                    )
                                    newItemText = ""
                                    newItemEstPrice = ""
                                    Toast.makeText(context, "Añadido a la lista", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Añadir")
                        }
                    }
                }
            }
        }

        // Low Stock alert suggestion banner
        if (lowStockItems.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BadgeReplenish.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📉", fontSize = 18.sp)
                            Text(
                                text = "${lowStockItems.size} productos bajos de inventario",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BadgeReplenish
                            )
                        }
                        Text(
                            text = "Productos en tu despensa que alcanzaron el stock mínimo de reposición.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    lowStockItems.forEach { item ->
                                        viewModel.addShoppingItem(
                                            name = item.name,
                                            qty = Math.max(1.0, item.minQty),
                                            unit = item.unit,
                                            estPrice = 0.0,
                                            source = "LOW_STOCK"
                                        )
                                    }
                                    Toast.makeText(context, "Productos por reponer agregados a la lista", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BadgeReplenish),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Añadir todos a la lista", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Shopping List Items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Artículos por comprar (${shoppingItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (checkedCount > 0) {
                    TextButton(onClick = { viewModel.clearCheckedShoppingItems() }) {
                        Text("Limpiar marcados", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (shoppingItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                        Text("Tu lista de compras está al día", fontWeight = FontWeight.Bold)
                        Text("Los faltantes de tus recetas y plan semanal aparecerán aquí.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(shoppingItems) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { isChecked -> viewModel.toggleShoppingItem(item.id, isChecked) },
                    onDelete = { viewModel.deleteShoppingItem(item.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingListItemEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = onToggle
                )
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Bold,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.qty} ${item.unit}" + if (item.estPrice > 0) " · ~RD$ ${String.format(java.util.Locale.US, "%.0f", item.estPrice)}" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
