package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.ColmadoDefaults
import com.example.data.model.EvaluatedRecipe
import com.example.data.model.Recipe
import com.example.ui.theme.BadgeOk
import com.example.ui.theme.BadgeReplenish
import com.example.ui.viewmodel.PantryViewModel
import com.example.ui.viewmodel.ScreenTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipesScreen(
    viewModel: PantryViewModel,
    onOpenColmadoPrices: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val pantryItems by viewModel.rawPantryItems.collectAsState()
    val config by viewModel.config.collectAsState()
    val filter by viewModel.recipeFilter.collectAsState()
    val servings by viewModel.recipeServings.collectAsState()

    val openRecipeCards = remember { mutableStateMapOf<Int, Boolean>() }

    // Evaluated recipes
    val evaluatedRecipes = remember(pantryItems, config.customPrices, servings) {
        Recipe.ALL.map { recipe ->
            val matches = recipe.ingredientPatterns.map { pattern ->
                recipe.hasIngredient(pattern, pantryItems)
            }
            val matchCount = matches.count { it }
            val proportion = matchCount.toDouble() / recipe.ingredientPatterns.size
            val costResult = recipe.calculateCost(servings, config.customPrices, pantryItems)
            EvaluatedRecipe(
                recipe = recipe,
                matches = matches,
                matchCount = matchCount,
                proportion = proportion,
                costResult = costResult
            )
        }
    }

    val filteredList = evaluatedRecipes.filter {
        when (filter) {
            "ready" -> it.proportion == 1.0
            "near" -> it.proportion in 0.6..0.99
            else -> true
        }
    }.sortedWith { a, b ->
        if (filter == "cheap") {
            a.costResult.missingCost.compareTo(b.costResult.missingCost)
        } else {
            b.proportion.compareTo(a.proportion)
        }
    }

    val readyCount = evaluatedRecipes.count { it.proportion == 1.0 }
    val nearCount = evaluatedRecipes.count { it.proportion in 0.6..0.99 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectTab(ScreenTab.PANTRY) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Column {
                        Text(
                            text = "MANAGEMENT TECH GEO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "🍳 ¿Qué cocino hoy?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                com.example.ui.components.PermanentProBadge(
                    isProUser = config.isProUser,
                    onOpenPaywall = onOpenPaywall
                )
            }

            Text(
                text = "Recetas sugeridas con los alimentos reales de tu despensa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }

        // Dedicated Chef Ideas Banner
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFF8E7),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTab(ScreenTab.CHEF_IDEAS) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "👨‍🍳", fontSize = 30.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Qué puedo cocinar con mis productos?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A5800)
                        )
                        Text(
                            text = "Selecciona tus ingredientes y recibe ideas con tips criollos y pasos detallados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4A3B18)
                        )
                    }
                    Button(
                        onClick = { viewModel.selectTab(ScreenTab.CHEF_IDEAS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF37),
                            contentColor = Color(0xFF1B2A20)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Abrir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Summary Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val summaryText = when {
                        readyCount > 0 -> "📝 Con lo que tienes guardado ya puedes cocinar: ${
                            evaluatedRecipes.filter { it.proportion == 1.0 }
                                .take(4).joinToString(", ") { it.recipe.name }
                        }${if (readyCount > 4) " y ${readyCount - 4} más" else ""}."
                        nearCount > 0 -> "📝 Te falta poco para completar recetas como: ${
                            evaluatedRecipes.filter { it.proportion in 0.6..0.99 }
                                .take(3).joinToString("; ") { it.recipe.name }
                        }."
                        else -> "📝 Añade alimentos a tu despensa (arroz, huevos, plátano, pollo...) para desbloquear recetas automáticas."
                    }
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )

                    // Budget highlight
                    val cheapest = evaluatedRecipes.minByOrNull { it.costResult.missingCost }
                    if (cheapest != null) {
                        Text(
                            text = "💰 Presupuesto para $servings personas: lo más económico de completar hoy es ${cheapest.recipe.name} (faltaría comprar ${ColmadoDefaults.formatRD(cheapest.costResult.missingCost)}).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Servings & Colmado Prices Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Para:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                listOf(2, 4, 6).forEach { n ->
                    FilterChip(
                        selected = servings == n,
                        onClick = { viewModel.setRecipeServings(n) },
                        label = { Text("$n personas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Button(
                    onClick = onOpenColmadoPrices,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Precios colmado", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "all" to "Todas (${evaluatedRecipes.size})",
                    "ready" to "Puedo hacerlas ya ($readyCount)",
                    "near" to "Me falta poco ($nearCount)",
                    "cheap" to "💸 Más baratas"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = filter == key,
                        onClick = { viewModel.setRecipeFilter(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        // Recipe Cards
        items(filteredList, key = { it.recipe.id }) { item ->
            val recipe = item.recipe
            val isExpanded = openRecipeCards[recipe.id] ?: false

            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = recipe.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recipe.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = "${recipe.time} · ${item.matchCount}/${recipe.ingredientPatterns.size} ingredientes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Availability badge
                        if (item.proportion == 1.0) {
                            Surface(
                                color = BadgeOk.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BadgeOk, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lista", color = BadgeOk, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            val missingCount = recipe.ingredientPatterns.size - item.matchCount
                            Surface(
                                color = BadgeReplenish.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Faltan $missingCount",
                                    color = BadgeReplenish,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Cost summary block
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💰 Colmado: ${ColmadoDefaults.formatRD(item.costResult.totalCost)} (aprox. ${ColmadoDefaults.formatRD(item.costResult.totalCost / servings)}/persona)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (item.costResult.missingCost > 0.0 && item.costResult.missingCost < item.costResult.totalCost) {
                                Text(
                                    text = "🛒 Te faltaría comprar: ${ColmadoDefaults.formatRD(item.costResult.missingCost)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Ingredients checklist chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        recipe.ingredientPatterns.forEachIndexed { idx, pattern ->
                            val have = item.matches.getOrElse(idx) { false }
                            val cleanName = pattern.split("|")[0].replaceFirstChar { it.uppercase() }
                            Surface(
                                color = if (have) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50),
                                border = if (have) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (have) "✅ $cleanName" else "⬜ $cleanName",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (have) FontWeight.Bold else FontWeight.Normal,
                                        color = if (have) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { openRecipeCards[recipe.id] = !isExpanded },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isExpanded) "Ocultar" else "Ver preparación")
                        }

                        if (item.proportion < 1.0) {
                            Button(
                                onClick = {
                                    val added = viewModel.addMissingRecipeIngredients(recipe.id)
                                    Toast.makeText(
                                        context,
                                        if (added > 0) "$added ingredientes añadidos a tu lista" else "Ya estaban en tu lista",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir faltantes")
                            }
                        }
                    }

                    // Step-by-step instructions & cost details if opened
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💰 Desglose para $servings personas:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            item.costResult.lines.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${line.name} (${line.quantityDisplay})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = if (line.haveInPantry) "✅ Lo tienes" else ColmadoDefaults.formatRD(line.cost),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (line.haveInPantry) BadgeOk else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "👨‍🍳 Paso a paso:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            recipe.instructions.forEachIndexed { index, step ->
                                Text(
                                    text = "${index + 1}. $step",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
