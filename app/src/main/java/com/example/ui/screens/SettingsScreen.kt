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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppColorTheme
import com.example.data.local.MonetizationConstants
import com.example.data.local.SortOption
import com.example.data.local.ThemeModeOption
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeOk
import com.example.ui.viewmodel.LimitType
import com.example.ui.viewmodel.PantryViewModel

@Composable
fun SettingsScreen(
    viewModel: PantryViewModel,
    onOpenColmadoPrices: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileEmoji by remember { mutableStateOf("🏠") }

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
                Column {
                    Text(
                        text = "MANAGEMENT TECH GEO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        text = "Ajustes",
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

        // Pro Lifetime Promotion / Status Card
        item {
            if (config.isProUser) {
                ElevatedCard(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BadgeOk,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Despensa Pro Activo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Licencia vitalicia verificada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "Tienes acceso completo a todas las funciones ilimitadas: productos infinitos, escaneos mensuales sin tope, reportes históricos y múltiples perfiles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.restorePurchases()
                                    Toast.makeText(context, "Estado de compra sincronizado con Google Play", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verificar compra")
                            }
                        }
                    }
                }
            } else {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37),
                                    Color(0xFFB4552D),
                                    MaterialTheme.colorScheme.primary
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "👑", fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = "Desbloquear Pro",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Acceso Ilimitado de por Vida",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = MonetizationConstants.PRO_PRICE_RD,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Pago único – Acceso de por vida",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Beneficios incluidos en tu licencia permanente:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProBenefitRow("✓", "Productos ilimitados (elimina el límite de 30)")
                            ProBenefitRow("✓", "Escaneos de facturas ilimitados (sin tope de 5 al mes)")
                            ProBenefitRow("✓", "Historial y reportes completos de todos los meses")
                            ProBenefitRow("✓", "Multi-usuario: gestiona múltiples hogares y perfiles")
                            ProBenefitRow("✓", "Chef Inteligente con ideas y tips culinarios")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenPaywall,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_unlock_pro_button")
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Desbloquear Pro (${MonetizationConstants.PRO_PRICE_RD})",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.restorePurchases()
                                    Toast.makeText(context, "Buscando compras previas en Google Play...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Restaurar")
                            }
                        }
                    }
                }
            }
        }

        // Multi-Profile Management (1 Profile in Free, Multi in Pro)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Perfiles y despensas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!config.isProUser) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "1 usuario en gratis",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = if (config.isProUser) {
                            "Cambia entre diferentes despensas o perfiles de tu familia o negocio."
                        } else {
                            "La versión gratuita incluye 1 perfil. Con Despensa Pro puedes crear múltiples perfiles y despensas compartidas."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        config.profiles.forEach { profile ->
                            val isSelected = profile.id == config.currentProfileId
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val ok = viewModel.trySwitchProfile(profile.id)
                                        if (ok) {
                                            Toast.makeText(context, "Perfil activo: ${profile.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = profile.emoji, fontSize = 22.sp)
                                        Column {
                                            Text(
                                                text = profile.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (profile.id == "p_main") {
                                                Text(
                                                    text = "Perfil principal (Gratis)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            } else {
                                                Text(
                                                    text = "Perfil secundario (Pro)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Activo",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else if (!config.isProUser) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Bloqueado",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (config.isProUser) {
                                showAddProfileDialog = true
                            } else {
                                viewModel.showLimitDialog(LimitType.PROFILE_LIMIT)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (config.isProUser) Icons.Default.Add else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (config.isProUser) "Añadir nuevo perfil" else "Desbloquear multi-usuario (Pro)")
                    }
                }
            }
        }

        // Color Theme Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🎨 Tema de color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppColorTheme.entries.forEach { theme ->
                            val isSelected = config.colorTheme == theme
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.setColorTheme(theme) }
                                    .padding(8.dp)
                                    .width(76.dp)
                                    .testTag("theme_button_${theme.key}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(theme.primaryHex),
                                                    Color(theme.secondaryHex)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Appearance Mode
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🌗 Apariencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Modo de visualización",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeModeOption.entries.forEach { mode ->
                            FilterChip(
                                selected = config.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Pantry Name & Preferences
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🏠 Mi despensa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = config.pantryName,
                        onValueChange = { viewModel.setPantryName(it) },
                        label = { Text("Nombre (aparece en Inicio)") },
                        placeholder = { Text("Ej. Familia Pérez, Colmado Geo...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_pantry_name_input"),
                        singleLine = true
                    )

                    Text(
                        text = "Avisar de vencimientos con:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(3, 7, 15, 30).forEach { days ->
                            FilterChip(
                                selected = config.warningDays == days,
                                onClick = { viewModel.setWarningDays(days) },
                                label = { Text("$days días") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = "Ordenar la despensa por:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SortOption.entries.forEach { sort ->
                            FilterChip(
                                selected = config.sortOption == sort,
                                onClick = { viewModel.setSortOption(sort) },
                                label = { Text(sort.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Colmado Prices Editor CTA
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💰 Precios del colmado (RD$)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ajusta los precios de referencia de Santo Domingo para calcular el costo real de tus recetas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenColmadoPrices,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("settings_open_prices_button")
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver y ajustar precios")
                    }
                }
            }
        }

        // Backup and Restore
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💾 Respaldo y datos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Exporta tus datos en JSON para tener una copia de seguridad o transferirlos a otro dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportBackupJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Despensa Pro Respaldo", json))
                                Toast.makeText(context, "Respaldo JSON copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar respaldo")
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restaurar")
                        }
                    }
                }
            }
        }

        // Maintenance
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🧹 Mantenimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearCompletedReminders()
                                Toast.makeText(context, "Recordatorios completados eliminados", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Limpiar hechos")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearHistory()
                                Toast.makeText(context, "Historial borrado", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Borrar historial")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Borrar todos mis datos")
                    }
                }
            }
        }

        // WhatsApp Support
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💬 ¿Necesitas asistencia o información?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "WhatsApp: 809-983-1578",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            val uri = Uri.parse("https://wa.me/18099831578?text=${Uri.encode("Hola, tengo una consulta sobre Despensa Pro")}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escribir por WhatsApp")
                    }
                }
            }
        }

        // Credits Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ Creado por MANAGEMENT TECH GEO ✨",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Despensa Pro v1.0 · República Dominicana",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    // Add Profile Dialog
    if (showAddProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = { Text("Añadir nuevo perfil / despensa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Crea una despensa separada para otro hogar, casa de playa o negocio.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Nombre del perfil") },
                        placeholder = { Text("Ej. Casa de campo, Negocio") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("🏠", "🌴", "🏢", "👶", "🛒").forEach { emoji ->
                            FilterChip(
                                selected = newProfileEmoji == emoji,
                                onClick = { newProfileEmoji = emoji },
                                label = { Text(emoji, fontSize = 18.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.tryAddProfile(newProfileName.trim(), newProfileEmoji)
                            showAddProfileDialog = false
                            newProfileName = ""
                            Toast.makeText(context, "Nuevo perfil creado", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restaurar respaldo JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pega aquí el texto JSON exportado previamente. Reemplazará los datos actuales de la despensa.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importBackupJson(restoreJsonText)
                        if (success) {
                            Toast.makeText(context, "Respaldo restaurado exitosamente", Toast.LENGTH_SHORT).show()
                            showRestoreDialog = false
                        } else {
                            Toast.makeText(context, "El formato JSON no es válido", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Borrar todos los datos?") },
            text = {
                Text("Esta acción eliminará todos los productos, recordatorios e historial de la aplicación. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        Toast.makeText(context, "Todos los datos han sido borrados", Toast.LENGTH_SHORT).show()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProBenefitRow(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = icon,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
