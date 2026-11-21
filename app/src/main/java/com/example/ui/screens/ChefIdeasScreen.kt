package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeOk
import com.example.ui.viewmodel.PantryViewModel
import com.example.ui.viewmodel.ScreenTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChefIdeasScreen(
    viewModel: PantryViewModel,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val rawItems by viewModel.rawPantryItems.collectAsState()
    val selectedProducts by viewModel.selectedProductsForChef.collectAsState()
    val generatedIdeas by viewModel.generatedChefIdeas.collectAsState()
    val config by viewModel.config.collectAsState()

    val foodItems = remember(rawItems) {
        rawItems.filter { it.cat == Category.ALIMENTO && it.qty > 0 }
    }

    val openIdeaCards = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(foodItems) {
        if (selectedProducts.isEmpty()) {
            viewModel.selectAllFoodProductsForChef()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectTab(ScreenTab.RECIPES) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Text(
                        text = "Chef Inteligente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                PermanentProBadge(
                    isProUser = config.isProUser,
                    onOpenPaywall = onOpenPaywall
                )
            }

            Text(
                text = "🍳 ¿Qué puedo cocinar con estos productos?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Elige uno o varios productos de tu despensa y te daremos ideas personalizadas con tips y preparación detallada paso a paso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Product Selector Box
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tus ingredientes (${selectedProducts.size}/${foodItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { viewModel.selectAllFoodProductsForChef() }) {
                                Text("Todos", style = MaterialTheme.typography.labelMedium)
                            }
                            TextButton(onClick = { viewModel.clearChefSelectedProducts() }) {
                                Text("Limpiar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (foodItems.isEmpty()) {
                        Text(
                            text = "No tienes alimentos registrados en tu despensa. Añade algunos productos para recibir sugerencias de cocina.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
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
                                    label = { Text("${item.cat.emoji} ${item.name}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.generateChefIdeas() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_chef_ideas_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generar ideas con estos productos", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Generated Ideas Section
        if (generatedIdeas.isNotEmpty()) {
            item {
                Text(
                    text = "💡 Ideas y recetas sugeridas (${generatedIdeas.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(generatedIdeas, key = { it.id }) { idea ->
                val isExpanded = openIdeaCards[idea.id] ?: true

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
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = idea.emoji, fontSize = 34.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = idea.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "${idea.time} · Dificultad: ${idea.difficulty}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        // Matched Ingredients Chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            idea.matchedIngredients.forEach { ing ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "✓ $ing",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Expand / Collapse Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { openIdeaCards[idea.id] = !isExpanded }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "Ocultar tips e instrucciones" else "Ver tips de cocina e instrucciones detalladas",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Detailed Tips and Instructions
                        AnimatedVisibility(visible = isExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Chef Tips Block
                                Surface(
                                    color = Color(0xFFFFF8E7),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = Color(0xFFD4AF37),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "💡 Tips y Secretos de Cocina Dominicana",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF7A5800)
                                            )
                                        }
                                        idea.chefTips.forEach { tip ->
                                            Text(
                                                text = tip,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF4A3B18),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }

                                // Step-by-Step Instructions Block
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "👨‍🍳 Instrucciones Detalladas Paso a Paso",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        idea.detailedInstructions.forEachIndexed { idx, step ->
                                            Text(
                                                text = "${idx + 1}. $step",
                                                style = MaterialTheme.typography.bodySmall,
                                                lineHeight = 19.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Additional Ingredients
                                if (idea.optionalAdditions.isNotEmpty()) {
                                    Text(
                                        text = "🛒 Ingredientes adicionales recomendados: ${idea.optionalAdditions.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
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
