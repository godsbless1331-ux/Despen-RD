package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MonetizationConstants
import com.example.ui.theme.BadgeOk

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onBuy: (Activity) -> Unit,
    onRestore: () -> Unit,
    onUnlockWithCode: ((String) -> Boolean)? = null,
    onActivateDemo: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showCodeInput by remember { mutableStateOf(false) }
    var enteredCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "✨ OFERTA DE LANZAMIENTO",
                            color = Color(0xFFB8860B),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_paywall_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Crown / Star Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFDF00), Color(0xFFD4AF37))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Desbloquear Despensa Pro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Potencia tu hogar o colmado con acceso total sin límites ni interrupciones.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Price Card (RD$1,690 Lifetime)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAGO ÚNICO · ACCESO DE POR VIDA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB8860B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = MonetizationConstants.PRO_PRICE_RD,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Sin mensualidades ni cobros recurrentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Checklist of Benefits (✓)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Beneficios incluidos en tu compra:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    ProBenefitItem(
                        title = "Productos ilimitados en la despensa",
                        subtitle = "Elimina permanentemente el límite de 30 productos."
                    )
                    ProBenefitItem(
                        title = "Escaneo ilimitado de facturas",
                        subtitle = "Escanea todos los tickets de colmados y supermercados que desees cada mes."
                    )
                    ProBenefitItem(
                        title = "Historial y reportes completos de todos los meses",
                        subtitle = "Analiza tus gastos, movimientos y mermas sin restricción de solo el día actual."
                    )
                    ProBenefitItem(
                        title = "Multi-usuario y perfiles familiares",
                        subtitle = "Gestiona múltiples despensas (Hogar, Colmado, Negocio, Casa de campo)."
                    )
                    ProBenefitItem(
                        title = "Modo Chef con ideas de cocina y tips detallados",
                        subtitle = "Genera ideas a la medida con trucos criollos e instrucciones paso a paso."
                    )
                    ProBenefitItem(
                        title = "Sin publicidad ni límites futuros",
                        subtitle = "Acceso a todas las futuras actualizaciones y soporte prioritario por WhatsApp."
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Purchase Button (Google Play Billing)
                Button(
                    onClick = {
                        if (activity != null) {
                            onBuy(activity)
                        } else {
                            onActivateDemo()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("paywall_buy_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color(0xFF1B2A20)
                    )
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Comprar por ${MonetizationConstants.PRO_PRICE_RD}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                // Password / Secret Code VIP Unlock Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCodeInput = !showCodeInput },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "¿Tienes una contraseña o clave VIP?",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (showCodeInput) "Ocultar" else "Canjear",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(visible = showCodeInput) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Ingresa la clave maestra o contraseña de activación:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = enteredCode,
                                    onValueChange = {
                                        enteredCode = it
                                        codeError = null
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pro_unlock_code_input"),
                                    isError = codeError != null
                                )

                                if (codeError != null) {
                                    Text(
                                        text = codeError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (enteredCode.isBlank()) {
                                            codeError = "Por favor ingresa la contraseña."
                                            return@Button
                                        }
                                        val ok = onUnlockWithCode?.invoke(enteredCode) ?: false
                                        if (ok) {
                                            Toast.makeText(
                                                context,
                                                "¡Clave VIP aceptada! Despensa Pro desbloqueada de por vida.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onDismiss()
                                        } else {
                                            codeError = "Contraseña incorrecta. Verifica tu clave VIP."
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pro_unlock_code_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Desbloquear con Clave VIP")
                                }
                            }
                        }
                    }
                }

                // Secondary actions: Restore purchases
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onRestore, modifier = Modifier.testTag("paywall_restore_button")) {
                        Text("Restaurar compra previa de Google Play", style = MaterialTheme.typography.bodySmall)
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Continuar con versión gratuita",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ProBenefitItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BadgeOk,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = "✓ $title",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
