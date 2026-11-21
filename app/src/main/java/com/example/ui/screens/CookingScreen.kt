package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.Category
import com.example.data.model.ChefIdea
import com.example.data.model.EvaluatedRecipe
import com.example.data.model.MealSlot
import com.example.data.model.Recipe
import com.example.data.model.RecipeMealType
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeExpired
import com.example.ui.theme.BadgeOk
import com.example.ui.theme.BadgeReplenish
import com.example.ui.viewmodel.LimitType
import com.example.ui.viewmodel.PantryViewModel
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CookingScreen(
    viewModel: PantryViewModel,
    onOpenColmadoPrices: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val pantryItems by viewModel.rawPantryItems.collectAsState()
    val config by viewModel.config.collectAsState()
    val filter by viewModel.recipeFilter.collectAsState()
    val mealFilter by viewModel.recipeMealFilter.collectAsState()
    val servings by viewModel.recipeServings.collectAsState()
    val mealPlan by viewModel.mealPlanEntries.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Recetas, 1: Chef IA, 2: Plan Semanal

    val openRecipeCards = remember { mutableStateMapOf<Int, Boolean>() }
    val openChefCards = remember { mutableStateMapOf<String, Boolean>() }

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
        }.sortedByDescending { it.proportion }
    }

    val readyCount = evaluatedRecipes.count { it.proportion == 1.0 }
    val breakfastCount = evaluatedRecipes.count { RecipeMealType.DESAYUNO in it.recipe.mealTypes }
    val dinnerCount = evaluatedRecipes.count { RecipeMealType.CENA in it.recipe.mealTypes }

    val todayDayOfWeek = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
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
                        text = "Cocina Criolla & Chef",
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

        // Sub-tabs (Recetas, Chef IA, Plan Semanal)
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("🍲 Recetas ($readyCount)") },
                    modifier = Modifier.testTag("cooking_tab_recipes")
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("👨‍🍳 Chef IA") },
                    modifier = Modifier.testTag("cooking_tab_chef")
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = {
                        if (!config.isProUser) {
                            viewModel.showLimitDialog(LimitType.REPORT_HISTORY_LIMIT)
                        } else {
                            selectedSubTab = 2
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📅 Plan")
                            if (!config.isProUser) {
                                Text("👑", fontSize = 12.sp)
                            }
                        }
                    },
                    modifier = Modifier.testTag("cooking_tab_plan")
                )
            }
        }

        // CONTENT BASED ON SUB-TAB
        when (selectedSubTab) {
            0 -> {
                // RECETARIO CRIOLLO SECTION
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "¿Qué vas a preparar hoy?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Meal Type Filter Chips (Todas, Desayuno, Cena, Almuerzo)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = mealFilter == null,
                                onClick = { viewModel.setRecipeMealFilter(null) },
                                label = { Text("🍽️ Todas (${evaluatedRecipes.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("meal_filter_all")
                            )
                            FilterChip(
                                selected = mealFilter == RecipeMealType.DESAYUNO,
                                onClick = { viewModel.setRecipeMealFilter(RecipeMealType.DESAYUNO) },
                                label = { Text("🌅 Desayuno ($breakfastCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("meal_filter_desayuno")
                            )
                            FilterChip(
                                selected = mealFilter == RecipeMealType.CENA,
                                onClick = { viewModel.setRecipeMealFilter(RecipeMealType.CENA) },
                                label = { Text("🌙 Cena ($dinnerCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("meal_filter_cena")
                            )
                            FilterChip(
                                selected = mealFilter == RecipeMealType.ALMUERZO,
                                onClick = { viewModel.setRecipeMealFilter(RecipeMealType.ALMUERZO) },
                                label = { Text("☀️ Almuerzo") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("meal_filter_almuerzo")
                            )
                        }

                        // Availability Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = filter == "all",
                                onClick = { viewModel.setRecipeFilter("all") },
                                label = { Text("Todas las recetas") }
                            )
                            FilterChip(
                                selected = filter == "ready",
                                onClick = { viewModel.setRecipeFilter("ready") },
                                label = { Text("✅ Listas con lo que tengo") }
                            )
                            FilterChip(
                                selected = filter == "near",
                                onClick = { viewModel.setRecipeFilter("near") },
                                label = { Text("🛒 Casi listas") }
                            )
                        }

                        // Servings and Colmado prices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Personas:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                listOf(2, 4, 6).forEach { s ->
                                    FilterChip(
                                        selected = servings == s,
                                        onClick = { viewModel.setRecipeServings(s) },
                                        label = { Text("$s") }
                                    )
                                }
                            }

                            TextButton(onClick = onOpenColmadoPrices) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Precios colmado", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                val filteredList = evaluatedRecipes.filter { eval ->
                    val matchesMeal = mealFilter == null || mealFilter in eval.recipe.mealTypes
                    val matchesAvail = when (filter) {
                        "ready" -> eval.proportion == 1.0
                        "near" -> eval.proportion in 0.5..0.999
                        else -> true
                    }
                    matchesMeal && matchesAvail
                }

                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🍽️", fontSize = 32.sp)
                                Text(
                                    text = "No hay recetas listas con este filtro",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(onClick = { viewModel.setRecipeFilter("all") }) {
                                    Text("Ver todas las recetas")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.recipe.id }) { item ->
                        val isExpanded = openRecipeCards[item.recipe.id] ?: false
                        RecipeCard(
                            item = item,
                            servings = servings,
                            isExpanded = isExpanded,
                            onToggle = { openRecipeCards[item.recipe.id] = !isExpanded },
                            onAddMissing = {
                                val count = viewModel.addMissingRecipeIngredients(item.recipe.id)
                                Toast.makeText(
                                    context,
                                    if (count > 0) "$count ingredientes añadidos a la lista de compras" else "Ya tienes los ingredientes en la lista",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onAddToPlan = { slot ->
                                if (!config.isProUser) {
                                    viewModel.showLimitDialog(LimitType.REPORT_HISTORY_LIMIT)
                                } else {
                                    viewModel.setMealPlanEntry(todayDayOfWeek, slot, item.recipe)
                                    Toast.makeText(
                                        context,
                                        "${item.recipe.name} agregado al ${slot.displayName} de hoy",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }

            1 -> {
                // CHEF INTELIGENTE SECTION
                item {
                    val foodItems = remember(pantryItems) {
                        pantryItems.filter { it.cat == Category.ALIMENTO && it.qty > 0 }
                    }
                    val selectedProducts by viewModel.selectedProductsForChef.collectAsState()
                    val chefMealFilter by viewModel.chefMealFilter.collectAsState()
                    val generatedIdeas by viewModel.generatedChefIdeas.collectAsState()

                    LaunchedEffect(foodItems) {
                        if (selectedProducts.isEmpty()) {
                            viewModel.selectAllFoodProductsForChef()
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "¿Qué puedes cocinar con tu despensa?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Elige el momento del día y marca los ingredientes disponibles para recibir recetas, tips y preparación paso a paso.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Meal time selector for Chef IA
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = chefMealFilter == null,
                                        onClick = { viewModel.setChefMealFilter(null) },
                                        label = { Text("🍽️ Todo") }
                                    )
                                    FilterChip(
                                        selected = chefMealFilter == RecipeMealType.DESAYUNO,
                                        onClick = { viewModel.setChefMealFilter(RecipeMealType.DESAYUNO) },
                                        label = { Text("🌅 Desayuno") }
                                    )
                                    FilterChip(
                                        selected = chefMealFilter == RecipeMealType.CENA,
                                        onClick = { viewModel.setChefMealFilter(RecipeMealType.CENA) },
                                        label = { Text("🌙 Cena") }
                                    )
                                    FilterChip(
                                        selected = chefMealFilter == RecipeMealType.ALMUERZO,
                                        onClick = { viewModel.setChefMealFilter(RecipeMealType.ALMUERZO) },
                                        label = { Text("☀️ Almuerzo") }
                                    )
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    foodItems.forEach { item ->
                                        val isSelected = selectedProducts.contains(item.name)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.toggleProductForChef(item.name) },
                                            label = { Text("${item.name} (${item.qty} ${item.unit})") },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { viewModel.selectAllFoodProductsForChef() }) {
                                        Text("Seleccionar todos")
                                    }
                                    Button(
                                        onClick = { viewModel.generateChefIdeas() },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Generar ideas")
                                    }
                                }
                            }
                        }

                        // Generated Chef Ideas
                        Text(
                            text = "Ideas y recetas recomendadas (${generatedIdeas.size}):",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        generatedIdeas.forEach { idea ->
                            val isExpanded = openChefCards[idea.id] ?: true
                            ChefIdeaCard(
                                idea = idea,
                                isExpanded = isExpanded,
                                onToggle = { openChefCards[idea.id] = !isExpanded }
                            )
                        }
                    }
                }
            }

            2 -> {
                // PLAN SEMANAL SECTION
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Planificación de Comidas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Organiza Desayuno, Almuerzo y Cena de la semana y envía los faltantes a tu lista.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val count = viewModel.sendMealPlanMissingsToShoppingList()
                                Toast.makeText(
                                    context,
                                    if (count > 0) "$count ingredientes faltantes enviados a tu lista de compras" else "¡Tienes todos los ingredientes en casa!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar faltantes de la semana a la Lista", fontWeight = FontWeight.Bold)
                        }

                        // Weekly days cards
                        (1..7).forEach { dayNum ->
                            val dayEntries = mealPlan.filter { it.dayOfWeek == dayNum }
                            val dayName = when (dayNum) {
                                1 -> "Lunes"
                                2 -> "Martes"
                                3 -> "Miércoles"
                                4 -> "Jueves"
                                5 -> "Viernes"
                                6 -> "Sábado"
                                7 -> "Domingo"
                                else -> "Día $dayNum"
                            }

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (dayEntries.isEmpty()) {
                                        Text(
                                            text = "Sin comida asignada aún",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    } else {
                                        dayEntries.forEach { entry ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(entry.slot.emoji)
                                                    Column {
                                                        Text(
                                                            text = "${entry.slot.displayName}: ${entry.recipeName}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "Para ${entry.servings} comensales",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { viewModel.toggleMealPlanCooked(entry.id, !entry.isCooked) }
                                                ) {
                                                    Icon(
                                                        if (entry.isCooked) Icons.Default.CheckCircle else Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = if (entry.isCooked) BadgeOk else MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeCard(
    item: EvaluatedRecipe,
    servings: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddMissing: () -> Unit,
    onAddToPlan: (MealSlot) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(item.recipe.emoji, fontSize = 30.sp)
                    Column {
                        Text(
                            text = item.recipe.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "⏱️ ${item.recipe.time} · Para $servings personas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "RD$ ${String.format(java.util.Locale.US, "%.0f", item.costResult.totalCost)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Meal type tags (Desayuno, Almuerzo, Cena)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item.recipe.mealTypes.forEach { meal ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${meal.emoji} ${meal.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Ingredient match status & toggle hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (item.proportion == 1.0) {
                        "✅ Tienes todos los ingredientes (${item.matchCount}/${item.recipe.ingredientPatterns.size})"
                    } else {
                        "${item.matchCount}/${item.recipe.ingredientPatterns.size} ingredientes en casa"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (item.proportion == 1.0) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.proportion == 1.0) BadgeOk else MaterialTheme.colorScheme.outline
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Ocultar" else "Ver receta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Menos" else "Ver receta",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Ingredientes necesarios (para $servings personas):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            item.costResult.lines.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (line.haveInPantry) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (line.haveInPantry) BadgeOk else BadgeExpired,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${line.name} · ${line.quantityDisplay}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (line.haveInPantry) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                    Text(
                                        text = if (line.haveInPantry) "En casa" else "~RD$ ${String.format(java.util.Locale.US, "%.0f", line.cost)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (line.haveInPantry) BadgeOk else BadgeReplenish
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Cómo se hace (Paso a paso):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    item.recipe.instructions.forEachIndexed { i, step ->
                        Text(
                            text = "${i + 1}. $step",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Actions inside expanded recipe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.proportion < 1.0) {
                            OutlinedButton(
                                onClick = onAddMissing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Faltantes a lista", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        val defaultSlot = when {
                            RecipeMealType.DESAYUNO in item.recipe.mealTypes -> MealSlot.DESAYUNO
                            RecipeMealType.CENA in item.recipe.mealTypes && RecipeMealType.ALMUERZO !in item.recipe.mealTypes -> MealSlot.CENA
                            else -> MealSlot.ALMUERZO
                        }
                        Button(
                            onClick = { onAddToPlan(defaultSlot) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Plan hoy (${defaultSlot.displayName})", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChefIdeaCard(
    idea: ChefIdea,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(idea.emoji, fontSize = 28.sp)
                    Column {
                        Text(
                            text = idea.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "⏱️ ${idea.time} · Dificultad: ${idea.difficulty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                idea.mealTypes.forEach { meal ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${meal.emoji} ${meal.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (idea.matchedIngredients.isNotEmpty() || idea.optionalAdditions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (idea.matchedIngredients.isNotEmpty()) {
                                    Text(
                                        text = "Ingredientes de tu despensa: ${idea.matchedIngredients.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BadgeOk
                                    )
                                }
                                if (idea.optionalAdditions.isNotEmpty()) {
                                    Text(
                                        text = "Opcionales para mejorar: ${idea.optionalAdditions.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Chef Tips Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF9E6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFC78200), modifier = Modifier.size(18.dp))
                                Text("Secretos de Chef Criollo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = Color(0xFF7A4F00))
                            }
                            idea.chefTips.forEach { tip ->
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A3508))
                            }
                        }
                    }

                    Text("Cómo se hace (Paso a paso):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    idea.detailedInstructions.forEachIndexed { i, step ->
                        Text(
                            text = "${i + 1}. $step",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
