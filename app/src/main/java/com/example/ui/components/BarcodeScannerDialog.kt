package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.Category
import com.example.data.model.PantryItem

@Composable
fun BarcodeScannerDialog(
    existingPantryItems: List<PantryItem>,
    onDismiss: () -> Unit,
    onBarcodeScanned: (PantryItem) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var manualCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun processCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.length < 4) {
            errorMessage = "Introduce un código de barras válido"
            return
        }

        val existing = existingPantryItems.find { it.barcode == trimmed }
        if (existing != null) {
            onBarcodeScanned(existing.copy(qty = 1.0))
            return
        }

        // Known Dominican Colmado barcode presets with canonical product details
        val preset = when (trimmed) {
            "746123456001" -> PantryItem(name = "Arroz Selecto Dominicano", qty = 1.0, unit = "lb", cat = Category.ALIMENTO, barcode = trimmed)
            "746123456002" -> PantryItem(name = "Habichuelas Rojas Don Juan", qty = 1.0, unit = "lb", cat = Category.ALIMENTO, barcode = trimmed)
            "746123456003" -> PantryItem(name = "Aceite Crisol 1L", qty = 1.0, unit = "l", cat = Category.ALIMENTO, barcode = trimmed)
            "746123456004" -> PantryItem(name = "Leche Rica Entera 1L", qty = 1.0, unit = "l", cat = Category.BEBIDA, barcode = trimmed)
            "746123456005" -> PantryItem(name = "Pañales Huggies Talla 3 (36 ud)", qty = 36.0, unit = "ud", cat = Category.BEBE, sub = "panales", barcode = trimmed)
            "746123456006" -> PantryItem(name = "Cloro Macier 1 Galón", qty = 1.0, unit = "l", cat = Category.LIMPIEZA, barcode = trimmed)
            else -> PantryItem(name = "", qty = 1.0, unit = "ud", cat = Category.ALIMENTO, barcode = trimmed)
        }

        onBarcodeScanned(preset)
    }

    // Scanning laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Escanear código",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (hasCameraPermission) {
                    IconButton(onClick = { isTorchOn = !isTorchOn }) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) Color(0xFFD4AF37) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Camera Viewfinder / Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }
                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.unbindAll()
                                        val camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview
                                        )
                                        camera.cameraControl.enableTorch(isTorchOn)
                                    } catch (_: Exception) {}
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Viewfinder box overlay
                    Box(
                        modifier = Modifier
                            .size(180.dp, 100.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Red laser scanner line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(2.dp)
                                .offset(y = laserOffset.dp)
                                .background(Color.Red.copy(alpha = 0.85f))
                        )
                    }

                    Text(
                        text = if (hasCameraPermission) "Apunta al código de barras del producto" else "Cámara no disponible, usa código manual",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }

                if (!hasCameraPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Activar permiso de cámara")
                    }
                }

                // Presets colmado dominicano
                Text(
                    text = "Códigos de prueba colmado RD:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = manualCode == "746123456001",
                        onClick = {
                            manualCode = "746123456001"
                            processCode("746123456001")
                        },
                        label = { Text("Arroz") }
                    )
                    FilterChip(
                        selected = manualCode == "746123456004",
                        onClick = {
                            manualCode = "746123456004"
                            processCode("746123456004")
                        },
                        label = { Text("Leche Rica") }
                    )
                    FilterChip(
                        selected = manualCode == "746123456005",
                        onClick = {
                            manualCode = "746123456005"
                            processCode("746123456005")
                        },
                        label = { Text("Pañales") }
                    )
                }

                OutlinedTextField(
                    value = manualCode,
                    onValueChange = {
                        manualCode = it
                        errorMessage = null
                    },
                    label = { Text("Número de código de barras") },
                    placeholder = { Text("Ej. 746123456001") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_manual_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { processCode(manualCode) },
                modifier = Modifier.testTag("barcode_confirm_button")
            ) {
                Text("Buscar producto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
