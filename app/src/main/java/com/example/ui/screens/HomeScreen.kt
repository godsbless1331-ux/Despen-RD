package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MonetizationConstants
import com.example.data.model.MovementType
import com.example.data.model.PantryItem
import com.example.data.model.ProfileKind
import com.example.data.model.RecipeMealType
import com.example.data.model.StockMovement
import com.example.ui.components.PantryCube3D
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeExpired
import com.example.ui.theme.BadgeExpiringSoon
import com.example.ui.theme.BadgeOk
import com.example.ui.theme.BadgeReplenish
import com.example.ui.viewmodel.LimitType
import com.example.ui.viewmodel.PantryViewModel
import com.example.ui.viewmodel.ScreenTab
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: PantryViewModel,
    onOpenScan: () -> Unit,
    onOpenManualAdd: () -> Unit,
    onEditItem: (PantryItem) -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.rawPantryItems.collectAsState()
    val movements by viewModel.history.collectAsState()
    val pastHistoryCount by viewModel.totalPastHistoryCount.collectAsState()
    val config by viewModel.config.collectAsState()
    val diaperDays by viewModel.babyDiaperDaysRemaining.collectAsState()
    val monthlyExpensesTotal by viewModel.currentMonthExpensesTotal.collectAsState()
    val mealPlan by viewModel.mealPlanEntries.collectAsState()

    val lowStockItems = items.filter { it.isLowStock }
    val expiringSoonItems = items.filter { it.isExpiringSoon(config.warningDays) || it.isExpired }

    val activeProfile = config.profiles.find { it.id == config.currentProfileId } ?: config.profiles.first()

    // Determine today's day of week: Calendar.MONDAY is 2, SUNDAY is 1
    val todayDayOfWeek = remember {
        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        when (calDay) {
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
    val todayMeals = remember(mealPlan, todayDayOfWeek) {
        mealPlan.filter { it.dayOfWeek == todayDayOfWeek }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar: Branding + Profile Indicator + Permanent Pro Purchase Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    // Profile Switcher Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (!config.isProUser) {
                                    viewModel.showLimitDialog(LimitType.PROFILE_LIMIT)
                                } else {
                                    viewModel.selectTab(ScreenTab.HOUSEHOLD)
                                }
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${activeProfile.emoji} ${activeProfile.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!config.isProUser) {
                            Text(
                                text = " · 1 perfil (Gratis)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                PermanentProBadge(
                    isProUser = config.isProUser,
                    onOpenPaywall = onOpenPaywall
                )
            }
        }

        // Hero Card ("Despensa RD") with 3D/4D Extruded Frame, Bevel Borders & Living Specular Light
        item {
            val heroPrimary = MaterialTheme.colorScheme.primary
            val heroSecondary = MaterialTheme.colorScheme.secondary
            val heroTertiary = MaterialTheme.colorScheme.tertiary
            val heroPlinthStart = lerp(heroPrimary, Color.Black, 0.45f)
            val heroPlinthEnd = lerp(heroTertiary, Color.Black, 0.45f)

            val hero4dTransition = rememberInfiniteTransition(label = "hero_4d")
            val sheenProgress by hero4dTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 5200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hero_sheen"
            )
            val haloPulse by hero4dTransition.animateFloat(
                initialValue = 0.22f,
                targetValue = 0.52f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cube_halo"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card")
            ) {
                // 3D Extruded Bottom Slab / Plinth
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 8.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = heroPrimary.copy(alpha = 0.5f),
                            spotColor = heroPrimary.copy(alpha = 0.65f)
                        )
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(heroPlinthStart, heroPlinthEnd)
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = lerp(heroPrimary, Color.Black, 0.65f),
                            shape = RoundedCornerShape(30.dp)
                        )
                )

                // Elevated Top Face of the 3D/4D Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    heroPrimary,
                                    lerp(heroPrimary, heroSecondary, 0.45f),
                                    heroTertiary
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1100f, 700f)
                            )
                        )
                        .drawBehind {
                            // 4D Diagonal Light Sheen Sweep
                            val sweepX = size.width * (sheenProgress * 1.4f - 0.2f)
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    ),
                                    start = Offset(sweepX - 240f, 0f),
                                    end = Offset(sweepX + 240f, size.height)
                                )
                            )
                            // Crisp inner top bevel highlight
                            drawLine(
                                color = Color.White.copy(alpha = 0.65f),
                                start = Offset(32f, 3f),
                                end = Offset(size.width - 32f, 3f),
                                strokeWidth = 3f
                            )
                        }
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.88f),
                                    Color.White.copy(alpha = 0.30f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Raised 3D Pill Badge above title
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.White.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = 0.55f)
                                    )
                                ) {
                                    Text(
                                        text = "✨ CENTRO DEL HOGAR 3D",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (config.pantryName.isNotBlank()) config.pantryName else "Despensa RD",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.45f),
                                            offset = Offset(0f, 4f),
                                            blurRadius = 8f
                                        )
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "El sistema operativo de tu hogar. Todo lo que tienes, cocinas y necesitas comprar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }

                            // 4D Glowing Pedestal Ring behind PantryCube3D
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = haloPulse),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = Color.White.copy(alpha = 0.45f),
                                        shape = CircleShape
                                    )
                            ) {
                                PantryCube3D(modifier = Modifier.size(94.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Tactile 3D Quick Action Push-Buttons (Escanear, Subir Manual, Cocina)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HeroPushButton3D(
                                label = "Escanear",
                                icon = {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                topColor = Color.White,
                                bottomColor = Color(0xFFE7ECF2),
                                lipColor = Color(0xFFB5C0CE),
                                contentColor = heroPrimary,
                                borderColor = Color.White,
                                testTag = "home_scan_barcode_button",
                                onClick = onOpenScan,
                                modifier = Modifier.weight(1f)
                            )

                            HeroPushButton3D(
                                label = "Manual",
                                icon = {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                topColor = Color.White.copy(alpha = 0.28f),
                                bottomColor = Color.White.copy(alpha = 0.16f),
                                lipColor = Color.Black.copy(alpha = 0.35f),
                                contentColor = Color.White,
                                borderColor = Color.White.copy(alpha = 0.65f),
                                testTag = "home_manual_add_button",
                                onClick = onOpenManualAdd,
                                modifier = Modifier.weight(1f)
                            )

                            HeroPushButton3D(
                                label = "Cocina",
                                icon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                topColor = Color(0xFFFFF6EB),
                                bottomColor = Color(0xFFEDDDC8),
                                lipColor = Color(0xFFB89E7E),
                                contentColor = Color(0xFF1B2A20),
                                borderColor = Color.White.copy(alpha = 0.9f),
                                testTag = "home_cooking_button",
                                onClick = { viewModel.selectTab(ScreenTab.COOKING) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Breakfast & Dinner Ideas Card
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🍳 ¿Qué cocinamos hoy?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Recetas con ingredientes exactos, tips y paso a paso.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openCookingForMeal(RecipeMealType.DESAYUNO) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_breakfast_button")
                        ) {
                            Text("🌅 Desayuno", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.openCookingForMeal(RecipeMealType.CENA) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_dinner_button")
                        ) {
                            Text("🌙 Cena", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Actionable Alert: Expiring Soon / Expired
        if (expiringSoonItems.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⏰", fontSize = 20.sp)
                            Text(
                                text = "Lotes por vencer (${expiringSoonItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Evita el desperdicio cocinando estos alimentos hoy o registrando su merma.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        expiringSoonItems.take(4).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${item.qty} ${item.unit} en ${item.location.displayName} · Exp: ${item.expDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.isExpired) BadgeExpired else BadgeExpiringSoon
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.clearChefSelectedProducts()
                                            viewModel.toggleProductForChef(item.name)
                                            viewModel.selectTab(ScreenTab.COOKING)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Cocinar", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.recordWaste(item.id, item.qty, "Vencido")
                                            Toast.makeText(context, "Merma registrada", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Merma", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Actionable Alert: Low Stock Items
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📉", fontSize = 20.sp)
                            Text(
                                text = "Bajos de stock (${lowStockItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        lowStockItems.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Quedan ${item.qty} ${item.unit} (mínimo: ${item.minQty})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BadgeReplenish
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.addShoppingItem(
                                            name = item.name,
                                            qty = Math.max(1.0, item.minQty),
                                            unit = item.unit,
                                            source = "LOW_STOCK"
                                        )
                                        Toast.makeText(context, "Añadido a lista de compras", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("A lista", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Meal (Plan Semanal)
        if (todayMeals.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Comida planificada para hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        todayMeals.forEach { meal ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${meal.slot.emoji} ${meal.slot.displayName}: ${meal.recipeName} (${meal.servings} p.)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(onClick = { viewModel.selectTab(ScreenTab.COOKING) }) {
                                    Text("Ver receta")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lente Bebé: Diapers Monitor
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(46.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Column {
                            Text("Calculadora de Pañales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (diaperDays > 0) "Alcanzan para aprox. $diaperDays días" else "Sin pañales registrados en despensa",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (diaperDays in 1..4) BadgeExpired else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (diaperDays <= 5) {
                        Button(
                            onClick = {
                                viewModel.addShoppingItem(name = "Pañales talla 3", qty = 1.0, unit = "paq", source = "BABY")
                                Toast.makeText(context, "Pañales añadidos a la lista", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Comprar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Household Monthly Finances Snapshot
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Finanzas del Hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Gastos del mes en colmado y súper", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "RD$ ${String.format(java.util.Locale.US, "%,.2f", monthlyExpensesTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val progress = if (config.monthlyBudgetRD > 0) Math.min(1.0f, (monthlyExpensesTotal / config.monthlyBudgetRD).toFloat()) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Meta presupuestaria: RD$ ${String.format(java.util.Locale.US, "%,.0f", config.monthlyBudgetRD)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Movements Ledger (Historial)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📜 Movimientos del Hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (!config.isProUser) {
                            Text("Hoy (Gratis)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    if (movements.isEmpty()) {
                        Text(
                            text = "Aún no hay movimientos registrados hoy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        movements.take(5).forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(m.type.emoji)
                                    Column {
                                        Text(m.itemName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${m.type.displayName} · ${m.delta} ${m.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (m.cost > 0) {
                                    Text(
                                        text = "RD$ ${String.format(java.util.Locale.US, "%.0f", m.cost)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (!config.isProUser && pastHistoryCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPaywall() }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = "$pastHistoryCount movimientos anteriores bloqueados en Gratis. Desbloquea Pro para historial completo.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroPushButton3D(
    label: String,
    icon: @Composable () -> Unit,
    topColor: Color,
    bottomColor: Color,
    lipColor: Color,
    contentColor: Color,
    borderColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        // 3D Bottom Lip
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 3.dp)
                .shadow(elevation = 6.dp, shape = shape)
                .clip(shape)
                .background(lipColor)
        )

        // Raised Push-Button Face
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp)
                .minimumInteractiveComponentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor)
                    )
                )
                .border(width = 1.4.dp, color = borderColor, shape = shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = onClick
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                icon()
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = contentColor,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp
                )
            }
        }
    }
}
