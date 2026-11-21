package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppColorTheme
import com.example.data.local.MonetizationConstants
import com.example.data.local.SortOption
import com.example.data.local.ThemeModeOption
import com.example.data.model.ProfileKind
import com.example.data.model.UserProfile
import com.example.ui.components.PermanentProBadge
import com.example.ui.theme.BadgeOk
import com.example.ui.viewmodel.LimitType
import com.example.ui.viewmodel.PantryViewModel

@Composable
fun HouseholdScreen(
    viewModel: PantryViewModel,
    onOpenColmadoPrices: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val monthlyExpensesTotal by viewModel.currentMonthExpensesTotal.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileEmoji by remember { mutableStateOf("🏠") }
    var newProfileKind by remember { mutableStateOf(ProfileKind.ADULT) }

    var exportedWebHtml by remember { mutableStateOf<String?>(null) }

    val saveHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        val html = exportedWebHtml
        if (uri != null && html != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(html.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Sitio web .html guardado correctamente", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo guardar el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var budgetText by remember { mutableStateOf(config.monthlyBudgetRD.toInt().toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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
                        text = "Hogar & Configuración",
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
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = CircleShape, color = BadgeOk, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                }
                            }
                            Column {
                                Text("Despensa Pro Activo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Licencia vitalicia verificada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(
                            text = "Acceso completo a productos infinitos, escaneos ilimitados, multi-usuario real, historial de todos los meses y respaldo total.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { viewModel.restorePurchases() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verificar compras Google Play")
                            }
                        }
                    }
                }
            } else {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFD4AF37), Color(0xFFB4552D), MaterialTheme.colorScheme.primary)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("👑", fontSize = 28.sp)
                                Column {
                                    Text("Desbloquear Pro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                    Text("Pago único – Acceso de por vida", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = MonetizationConstants.PRO_PRICE_RD,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("✓ Productos ilimitados (elimina el tope de 30)", style = MaterialTheme.typography.bodySmall)
                            Text("✓ Escaneos de facturas ilimitados", style = MaterialTheme.typography.bodySmall)
                            Text("✓ Modo multi-usuario y múltiples perfiles", style = MaterialTheme.typography.bodySmall)
                            Text("✓ Plan semanal y recetas con cálculo de costo", style = MaterialTheme.typography.bodySmall)
                            Text("✓ Respaldo simétrico de todos los datos", style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = onOpenPaywall,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Desbloquear Pro (${MonetizationConstants.PRO_PRICE_RD})", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenPaywall,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tengo una Contraseña / Clave VIP", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Real Multi-Profile Management
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Perfiles del Hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (!config.isProUser) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("1 usuario en gratis", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Elige qué despensa estás gestionando. Cambiar de perfil filtra los datos de inventario y compras.",
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
                                            Toast.makeText(context, "Despensa activa: ${profile.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(profile.emoji, fontSize = 22.sp)
                                        Column {
                                            Text(profile.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${profile.kind.displayName} ${if (profile.kind == ProfileKind.BABY) "· ${profile.diapersPerDay} pañales/día" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Activo", tint = MaterialTheme.colorScheme.primary)
                                    } else if (!config.isProUser) {
                                        Icon(Icons.Default.Lock, contentDescription = "Pro", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
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
                        Icon(if (config.isProUser) Icons.Default.Add else Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (config.isProUser) "Añadir nuevo perfil / despensa" else "Desbloquear multi-usuario (Pro)")
                    }
                }
            }
        }

        // Household Finances & Monthly Budget
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💰 Finanzas & Presupuesto del Hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Gestiona los gastos de alimentación y colmado del mes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Gastado este mes:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = "RD$ ${String.format(java.util.Locale.US, "%,.2f", monthlyExpensesTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Presupuesto meta:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = "RD$ ${String.format(java.util.Locale.US, "%,.2f", config.monthlyBudgetRD)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = budgetText,
                            onValueChange = { budgetText = it },
                            label = { Text("Ajustar Presupuesto Mensual (RD$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val b = budgetText.replace(',', '.').toDoubleOrNull() ?: 18000.0
                                viewModel.setMonthlyBudget(b)
                                Toast.makeText(context, "Presupuesto actualizado a RD$ $b", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text("Guardar")
                        }
                    }

                    TextButton(onClick = onOpenColmadoPrices) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver y ajustar precios del colmado")
                    }
                }
            }
        }

        // APK Android & Web Export Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📱 APK Android y 🌐 Sitio Web", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "• Versión Android (.apk / .aab): Esta app es nativa Android y puedes generar/descargar el instalador APK desde el menú de exportación de AI Studio.\n• Versión Sitio Web (.html): Exporta tu despensa, lista de compras y recetas (Desayuno, Almuerzo y Cena) en un archivo HTML5 listo para abrir en cualquier navegador o subir a Netlify Drop / GitHub Pages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            exportedWebHtml = viewModel.exportWebHtml()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_web_html_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generar Sitio Web (HTML)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Symmetric Backup & Data
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💾 Respaldo Simétrico de Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Exporta o restaura la totalidad de tus datos: inventario con ubicaciones, libro de movimientos contables, lista de compras y perfiles.",
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
                                clipboard.setPrimaryClip(ClipData.newPlainText("Despensa RD Respaldo", json))
                                Toast.makeText(context, "Respaldo JSON copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar todo")
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

        // Appearance & Themes
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎨 Tema y Apariencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

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
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(theme.primaryHex), Color(theme.secondaryHex))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(theme.displayName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Theme mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeModeOption.entries.forEach { mode ->
                            FilterChip(
                                selected = config.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode.displayName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Official Support
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
                    Text("💬 Asistencia & Consultas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("MANAGEMENT TECH GEO · WhatsApp: 809-983-1578", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = {
                            val uri = Uri.parse("https://wa.me/18099831578?text=${Uri.encode("Hola, tengo una consulta sobre Despensa RD")}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escribir por WhatsApp")
                    }
                }
            }
        }

        // Danger Zone: Reset
        item {
            OutlinedButton(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Borrar todos los datos locales")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Profile Dialog
    if (showAddProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = { Text("Añadir perfil o despensa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Crea una despensa separada para otro hogar, casa de campo o negocio.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Nombre del perfil") },
                        placeholder = { Text("Ej. Casa de Playa, Negocio...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("🏠", "🌴", "🏪", "👶", "🐾").forEach { emoji ->
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
                            viewModel.tryAddProfile(newProfileName.trim(), newProfileEmoji, newProfileKind)
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
                    Text("Pega aquí el texto JSON exportado previamente. Restaurará íntegramente inventario, lista y perfiles.", style = MaterialTheme.typography.bodySmall)
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
            text = { Text("Esta acción eliminará todos los productos, movimientos, lista de compras e historial. No se puede deshacer.") },
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

    // Exported Web HTML5 Dialog
    exportedWebHtml?.let { html ->
        AlertDialog(
            onDismissRequest = { exportedWebHtml = null },
            title = { Text("🌐 Sitio Web HTML5 listo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Se generó una página web interactiva con tu inventario actual, lista de compras y recetas de Desayuno, Almuerzo y Cena.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Cómo convertirlo en sitio web en línea:\n1. Toca 'Guardar archivo .html' (nómbralo index.html).\n2. Ábrelo directo en Chrome/Safari o súbelo gratis arrastrando la carpeta a Netlify Drop o GitHub Pages.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { saveHtmlLauncher.launch("index.html") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_web_html_file_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar archivo index.html")
                    }
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Despensa Pro Web HTML", html))
                            Toast.makeText(context, "Código HTML copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋 Copiar código HTML")
                    }
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Despensa Pro - Sitio Web HTML")
                                putExtra(Intent.EXTRA_TEXT, html)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir sitio web HTML"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir código")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { exportedWebHtml = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
