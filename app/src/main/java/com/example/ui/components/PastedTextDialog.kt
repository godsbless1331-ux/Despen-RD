package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PantryItem
import com.example.data.parser.ReceiptTextParser

@Composable
fun PastedTextDialog(
    onDismiss: () -> Unit,
    onAddItems: (List<PantryItem>, String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var parsedItems by remember { mutableStateOf<List<PantryItem>?>(null) }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (parsedItems == null) {
        // Step 1: Input text
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Pegar factura o lista",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pega aquí una lista o texto de factura. Una línea por producto (ej. «Arroz 5 lb», «2 Leche», «Cloro 1 l»).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = {
                            rawText = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text("Arroz 5 lb\nHabichuelas rojas 2 lb\nDetergente 1 ud\n6 Huevos")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("paste_text_area")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = ReceiptTextParser.parse(rawText)
                        if (parsed.isEmpty()) {
                            errorMessage = "No se detectaron productos válidos. Revisa el texto."
                        } else {
                            parsedItems = parsed
                            selectedIndices.clear()
                            selectedIndices.addAll(parsed.indices)
                        }
                    },
                    modifier = Modifier.testTag("parse_text_button")
                ) {
                    Text("Detectar productos")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    } else {
        // Step 2: Review and select items
        val currentItems = parsedItems ?: emptyList()
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Revisar productos (${selectedIndices.size}/${currentItems.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Desmarca los productos que no desees añadir a tu despensa:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        itemsIndexed(currentItems) { index, item ->
                            val isChecked = selectedIndices.contains(index)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIndices.add(index)
                                        else selectedIndices.remove(index)
                                    }
                                )
                                Text(
                                    text = "${item.cat.emoji} ${item.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.qty} ${item.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val itemsToAdd = selectedIndices.map { currentItems[it] }
                        if (itemsToAdd.isNotEmpty()) {
                            onAddItems(itemsToAdd, "Texto pegado")
                        }
                        onDismiss()
                    },
                    enabled = selectedIndices.isNotEmpty(),
                    modifier = Modifier.testTag("confirm_parsed_items_button")
                ) {
                    Text("Añadir a despensa (${selectedIndices.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = { parsedItems = null }) {
                    Text("Atrás")
                }
            }
        )
    }
}
