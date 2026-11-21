package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BabySubCategory
import com.example.data.model.Category
import com.example.data.model.PantryItem
import com.example.data.model.SUPPORTED_UNITS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    initialItem: PantryItem? = null,
    fromReminderId: String? = null,
    onDismiss: () -> Unit,
    onSave: (PantryItem, fromReminderId: String?) -> Unit,
    onSaveAndAddAnother: ((PantryItem, fromReminderId: String?) -> Unit)? = null,
    onDelete: ((String) -> Unit)? = null
) {
    val isNew = initialItem == null || initialItem.id.isBlank()

    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var qtyText by remember { mutableStateOf((initialItem?.qty ?: 1.0).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }) }
    var selectedUnit by remember { mutableStateOf(initialItem?.unit ?: "ud") }
    var selectedCategory by remember { mutableStateOf(initialItem?.cat ?: Category.ALIMENTO) }
    var selectedBabySub by remember {
        mutableStateOf(
            if (initialItem?.cat == Category.BEBE && !initialItem.sub.isNullOrBlank()) {
                initialItem.sub
            } else {
                BabySubCategory.guessSub(initialItem?.name ?: "").id
            }
        )
    }
    var minQtyText by remember { mutableStateOf((initialItem?.minQty ?: 0.0).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }) }
    var expDate by remember { mutableStateOf(initialItem?.expDate ?: "") }
    var barcode by remember { mutableStateOf(initialItem?.barcode ?: "") }
    var selectedLocation by remember { mutableStateOf(initialItem?.location ?: com.example.data.model.Location.PANTRY) }
    var brand by remember { mutableStateOf(initialItem?.brand ?: "") }

    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var catDropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun buildValidatedItem(): PantryItem? {
        if (name.isBlank()) {
            errorMessage = "Por favor escribe el nombre del producto"
            return null
        }
        val qty = qtyText.replace(',', '.').toDoubleOrNull()
        if (qty == null || qty < 0) {
            errorMessage = "La cantidad debe ser un número válido"
            return null
        }
        val minQty = minQtyText.replace(',', '.').toDoubleOrNull() ?: 0.0

        return (initialItem ?: PantryItem(name = name, qty = qty)).copy(
            name = name.trim(),
            qty = qty,
            unit = selectedUnit,
            cat = selectedCategory,
            location = selectedLocation,
            brand = brand.trim(),
            minQty = minQty,
            expDate = expDate.trim(),
            barcode = barcode.trim(),
            sub = if (selectedCategory == Category.BEBE) selectedBabySub else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Nuevo producto" else "Editar producto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                        if (selectedCategory == Category.BEBE && selectedBabySub.isBlank()) {
                            selectedBabySub = BabySubCategory.guessSub(it).id
                        }
                    },
                    label = { Text("Nombre del producto") },
                    placeholder = { Text("Ej. Arroz, cloro, pañales...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_qty_input"),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            SUPPORTED_UNITS.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = catDropdownExpanded,
                    onExpandedChange = { catDropdownExpanded = !catDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.emoji} ${selectedCategory.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catDropdownExpanded,
                        onDismissRequest = { catDropdownExpanded = false }
                    ) {
                        Category.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${cat.displayName}") },
                                onClick = {
                                    selectedCategory = cat
                                    catDropdownExpanded = false
                                    if (cat == Category.BEBE && selectedBabySub.isBlank()) {
                                        selectedBabySub = BabySubCategory.guessSub(name).id
                                    }
                                }
                            )
                        }
                    }
                }

                // Baby Subcategory if Category == BEBE
                if (selectedCategory == Category.BEBE) {
                    Text(
                        text = "Sección de bebé",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BabySubCategory.entries.forEach { sub ->
                            FilterChip(
                                selected = selectedBabySub == sub.id,
                                onClick = { selectedBabySub = sub.id },
                                label = { Text(sub.displayName) }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minQtyText,
                        onValueChange = { minQtyText = it },
                        label = { Text("Mínimo aviso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = expDate,
                        onValueChange = { expDate = it },
                        label = { Text("Vence (AAAA-MM-DD)") },
                        placeholder = { Text("2027-06-30") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                // Location Selector
                Text(
                    text = "Ubicación en el hogar:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.data.model.Location.entries.forEach { loc ->
                        FilterChip(
                            selected = selectedLocation == loc,
                            onClick = { selectedLocation = loc },
                            label = { Text("${loc.emoji} ${loc.displayName}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marca (opcional)") },
                    placeholder = { Text("Ej. Crisol, Goya, Rica, Macier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Código de barras (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isNew && onSaveAndAddAnother != null) {
                    OutlinedButton(
                        onClick = {
                            val item = buildValidatedItem() ?: return@OutlinedButton
                            onSaveAndAddAnother(item, fromReminderId)
                            name = ""
                            qtyText = "1"
                            minQtyText = "0"
                            expDate = ""
                            barcode = ""
                            brand = ""
                            errorMessage = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_and_add_another_button")
                    ) {
                        Text("Guardar y añadir otro producto")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = buildValidatedItem() ?: return@Button
                    onSave(item, fromReminderId)
                },
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!isNew && onDelete != null && initialItem != null) {
                    OutlinedButton(
                        onClick = { onDelete(initialItem.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.padding(end = 4.dp))
                        Text("Eliminar")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
