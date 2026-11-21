package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ColmadoDefaults

@Composable
fun ColmadoPricesDialog(
    customPrices: Map<String, Double>,
    onDismiss: () -> Unit,
    onSavePrices: (Map<String, Double>) -> Unit,
    onReset: () -> Unit
) {
    val editedPrices = remember {
        mutableStateMapOf<String, String>().apply {
            ColmadoDefaults.ITEMS.forEach { item ->
                val currentPrice = customPrices[item.key] ?: item.defaultPrice
                put(item.key, if (currentPrice % 1.0 == 0.0) currentPrice.toInt().toString() else currentPrice.toString())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "💰 Precios del colmado (RD$)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Referencia oficial mercado y colmados (RD$). Si tu colmado local tiene precios diferentes, cámbialos aquí y el costo de las recetas se recalculará automáticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ColmadoDefaults.ITEMS) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "por ${item.unit}${if (item.isEstimated) " · estimado" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            OutlinedTextField(
                                value = editedPrices[item.key] ?: "",
                                onValueChange = { editedPrices[item.key] = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("RD$") },
                                modifier = Modifier
                                    .width(115.dp)
                                    .testTag("price_input_${item.key}"),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val map = mutableMapOf<String, Double>()
                    editedPrices.forEach { (k, v) ->
                        val num = v.toDoubleOrNull()
                        if (num != null && num > 0) {
                            map[k] = num
                        }
                    }
                    onSavePrices(map)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_colmado_prices_button")
            ) {
                Text("Guardar precios")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        onReset()
                        onDismiss()
                    }
                ) {
                    Text("Restablecer")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
}
