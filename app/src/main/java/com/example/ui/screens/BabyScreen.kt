package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BabyGuideItem
import com.example.data.model.BabyGuides
import com.example.data.model.BabySubCategory
import com.example.data.model.Category
import com.example.data.model.PantryItem
import com.example.ui.theme.BadgeExpired
import com.example.ui.theme.BadgeExpiringSoon
import com.example.ui.theme.BadgeReplenish
import com.example.ui.viewmodel.PantryViewModel

@Composable
fun BabyScreen(
    viewModel: PantryViewModel,
    onOpenFormWithPreset: (PantryItem) -> Unit,
    onEditItem: (PantryItem) -> Unit
) {
    val allItems by viewModel.rawPantryItems.collectAsState()
    val config by viewModel.config.collectAsState()
    val selectedSub by viewModel.selectedBabySub.collectAsState()

    val babyItems = allItems.filter { it.cat == Category.BEBE }
    val diaperCount = babyItems
        .filter { it.sub == "panales" && it.unit == "ud" }
        .sumOf { it.qty }

    val daysLeft = if (config.diapersPerDay > 0) {
        (diaperCount / config.diapersPerDay).toInt()
    } else null

    val filteredBabyItems = babyItems.filter {
        selectedSub == "todo" || it.sub == selectedSub
    }

    val openGuides = remember { mutableStateMapOf<String, Boolean>() }
    var babyQuickGuideExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MANAGEMENT TECH GEO",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                text = "👶 Sección de bebé",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Pañales, toallitas, biberones y utensilios en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Diaper Estimator Hero
        item {
            ElevatedCard(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${diaperCount.toInt()} pañales",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = when {
                                    daysLeft == null -> "Indica cuántos usa al día para calcular cuánto te duran."
                                    daysLeft > 0 -> "Te alcanzan para unos $daysLeft días."
                                    else -> "Se acabaron o están por acabarse."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "👶", fontSize = 28.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pañales que usa al día:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        OutlinedTextField(
                            value = config.diapersPerDay.toString(),
                            onValueChange = {
                                val n = it.toIntOrNull()
                                if (n != null && n in 0..50) {
                                    viewModel.setDiapersPerDay(n)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier
                                .width(90.dp)
                                .testTag("diapers_per_day_input"),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "💡 Cuenta solo los pañales anotados en «ud». Si compras un paquete de 40, anota 40 ud.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Quick add buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onOpenFormWithPreset(
                            PantryItem(
                                name = "Pañales talla ",
                                qty = 1.0,
                                unit = "ud",
                                cat = Category.BEBE,
                                sub = "panales",
                                minQty = 10.0
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("＋ Pañales")
                }

                Button(
                    onClick = {
                        onOpenFormWithPreset(
                            PantryItem(
                                name = "Toallitas húmedas",
                                qty = 1.0,
                                unit = "paq",
                                cat = Category.BEBE,
                                sub = "toallitas",
                                minQty = 2.0
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("＋ Toallitas")
                }

                Button(
                    onClick = {
                        onOpenFormWithPreset(
                            PantryItem(
                                name = "",
                                qty = 1.0,
                                unit = "ud",
                                cat = Category.BEBE,
                                sub = "alimentacion"
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("＋ Alimentación")
                }

                Button(
                    onClick = {
                        onOpenFormWithPreset(
                            PantryItem(
                                name = "",
                                qty = 1.0,
                                unit = "ud",
                                cat = Category.BEBE,
                                sub = "utensilios"
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("＋ Utensilio")
                }
            }
        }

        // Quick 4-step tutorial card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { babyQuickGuideExpanded = !babyQuickGuideExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👶 Cómo usar esta sección",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (babyQuickGuideExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = babyQuickGuideExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. Guarda cada artículo con ＋ o desde Añadir eligiendo la categoría Bebé.", style = MaterialTheme.typography.bodySmall)
                            Text("2. Configura cuántos pañales usa al día y la app calcula cuántos días te duran en tiempo real.", style = MaterialTheme.typography.bodySmall)
                            Text("3. Define un mínimo de aviso (ej. 10 pañales) para recibir alertas antes de que se acaben.", style = MaterialTheme.typography.bodySmall)
                            Text("4. Abajo tienes las guías pediátricas paso a paso para el uso seguro de cada artículo.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Subcategory Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedSub == "todo",
                    onClick = { viewModel.setBabySubFilter("todo") },
                    label = { Text("Todo (${babyItems.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(50)
                )

                BabySubCategory.entries.forEach { sub ->
                    val count = babyItems.count { it.sub == sub.id }
                    FilterChip(
                        selected = selectedSub == sub.id,
                        onClick = { viewModel.setBabySubFilter(sub.id) },
                        label = { Text("${sub.displayName} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        // Baby Items List
        if (filteredBabyItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "👶", fontSize = 42.sp)
                        Text(
                            text = "Aún no hay productos de bebé en esta sección.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredBabyItems, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👶", fontSize = 26.sp, modifier = Modifier.padding(end = 10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${item.qty} ${item.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (item.isLowStock) {
                                Surface(
                                    color = BadgeReplenish.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "Por reponer",
                                        color = BadgeReplenish,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onEditItem(item) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Editar")
                        }
                    }
                }
            }
        }

        // Pediatrician-Aligned Guides
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📖 Cómo usar tus artículos de bebé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mini tutoriales paso a paso avalados por pediatría. Ante cualquier duda, consulta a tu pediatra.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    BabyGuides.ALL.forEachIndexed { index, guide ->
                        val isOpen = openGuides[guide.title] ?: (index == 0)
                        BabyGuideAccordion(
                            guide = guide,
                            isOpen = isOpen,
                            onToggle = { openGuides[guide.title] = !isOpen }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun BabyGuideAccordion(
    guide: BabyGuideItem,
    isOpen: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable { onToggle() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = guide.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(visible = isOpen) {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = guide.why,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                guide.steps.forEachIndexed { idx, step ->
                    Text(
                        text = "${idx + 1}. $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (guide.tip.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 ${guide.tip}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
