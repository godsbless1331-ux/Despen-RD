package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ConfidenceLevel
import com.example.data.model.Location
import com.example.data.model.ParsedReceiptLine
import com.example.data.model.ParsedReceiptResult
import com.example.ui.theme.BadgeExpired
import com.example.ui.theme.BadgeExpiringSoon
import com.example.ui.theme.BadgeOk

@Composable
fun ReceiptReviewDialog(
    initialResult: ParsedReceiptResult,
    onDismiss: () -> Unit,
    onConfirm: (storeName: String, lines: List<ParsedReceiptLine>, totalExpense: Double) -> Unit
) {
    var storeName by remember { mutableStateOf(initialResult.storeName) }
    val lines = remember {
        mutableStateListOf<ParsedReceiptLine>().apply {
            addAll(initialResult.lines)
        }
    }

    val activeCount = lines.count { it.isIncluded }
    val calculatedTotal = lines.filter { it.isIncluded }.sumOf { it.unitPrice }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Revisar Factura",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Verifica los productos antes de ingresar a la despensa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Store and Total Header Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Comercio:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total calculado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "RD$ ${String.format(java.util.Locale.US, "%,.2f", calculatedTotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$activeCount productos seleccionados",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Lines List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(lines) { index, item ->
                        ReceiptLineCard(
                            line = item,
                            onToggleInclude = {
                                lines[index] = item.copy(isIncluded = !item.isIncluded)
                            },
                            onUpdateName = { newName ->
                                lines[index] = item.copy(name = newName)
                            },
                            onUpdateQty = { newQty ->
                                lines[index] = item.copy(qty = newQty)
                            },
                            onUpdatePrice = { newPrice ->
                                lines[index] = item.copy(unitPrice = newPrice)
                            },
                            onDelete = {
                                lines.removeAt(index)
                            }
                        )
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Descartar")
                    }

                    Button(
                        onClick = {
                            val included = lines.filter { it.isIncluded }
                            onConfirm(storeName, included, calculatedTotal)
                        },
                        enabled = activeCount > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar ($activeCount)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLineCard(
    line: ParsedReceiptLine,
    onToggleInclude: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateQty: (Double) -> Unit,
    onUpdatePrice: (Double) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (line.isIncluded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (line.isIncluded) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Checkbox(
                        checked = line.isIncluded,
                        onCheckedChange = { onToggleInclude() }
                    )
                    // Confidence Pill
                    ConfidencePill(line.confidenceLevel)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }

            // Editable Name
            OutlinedTextField(
                value = line.name,
                onValueChange = onUpdateName,
                label = { Text("Nombre del producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Quantity, Unit & Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (line.qty % 1.0 == 0.0) line.qty.toInt().toString() else line.qty.toString(),
                    onValueChange = { s -> s.toDoubleOrNull()?.let { onUpdateQty(it) } },
                    label = { Text("Cant.") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = line.unit,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                OutlinedTextField(
                    value = String.format(java.util.Locale.US, "%.2f", line.unitPrice),
                    onValueChange = { s -> s.toDoubleOrNull()?.let { onUpdatePrice(it) } },
                    label = { Text("Precio RD$") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}

@Composable
private fun ConfidencePill(level: ConfidenceLevel) {
    val (bgColor, textColor, label) = when (level) {
        ConfidenceLevel.HIGH -> Triple(BadgeOk.copy(alpha = 0.2f), BadgeOk, "🟢 Alta")
        ConfidenceLevel.MEDIUM -> Triple(BadgeExpiringSoon.copy(alpha = 0.2f), BadgeExpiringSoon, "🟡 Revisar")
        ConfidenceLevel.LOW -> Triple(BadgeExpired.copy(alpha = 0.2f), BadgeExpired, "🔴 Dudoso")
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
