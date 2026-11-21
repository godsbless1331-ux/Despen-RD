package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PantryItem
import com.example.data.model.ParsedReceiptResult
import com.example.data.parser.ReceiptTextParser
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CaptureBottomSheet
import com.example.ui.components.CelebrationBurst
import com.example.ui.components.ColmadoPricesDialog
import com.example.ui.components.LimitAlertDialog
import com.example.ui.components.NavBar3D
import com.example.ui.components.NavDockItem
import com.example.ui.components.PastedTextDialog
import com.example.ui.components.PaywallDialog
import com.example.ui.components.ProductFormDialog
import com.example.ui.components.ReceiptReviewDialog
import com.example.ui.screens.CookingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HouseholdScreen
import com.example.ui.screens.PantryScreen
import com.example.ui.screens.ShoppingListScreen
import com.example.ui.theme.DespensaProTheme
import com.example.ui.viewmodel.PantryViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PantryViewModel = viewModel()
            val config by viewModel.config.collectAsState()

            DespensaProTheme(
                colorTheme = config.colorTheme,
                themeMode = config.themeMode
            ) {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

private data class NavItemData(
    val tab: ScreenTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainContent(viewModel: PantryViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val rawItems by viewModel.rawPantryItems.collectAsState()
    val config by viewModel.config.collectAsState()
    val showPaywall by viewModel.showPaywall.collectAsState()
    val activeLimitDialog by viewModel.activeLimitDialog.collectAsState()

    var burstTimestamp by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        viewModel.burstTrigger.collect { ts ->
            burstTimestamp = ts
        }
    }

    // Modal and sheet states
    var showCaptureSheet by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<PantryItem?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var activeReminderIdForForm by remember { mutableStateOf<String?>(null) }

    var showScanDialog by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showPricesDialog by remember { mutableStateOf(false) }

    // Human-in-the-loop receipt review state
    var pendingReceiptResult by remember { mutableStateOf<ParsedReceiptResult?>(null) }

    // Photo picker for receipts (human review dialog before database commit)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            if (!viewModel.canScanReceipt()) {
                return@rememberLauncherForActivityResult
            }
            // Parse sample ticket lines representing supermarket purchase
            val sampleReceiptTicket = """
                Supermercado Sirena Churchill
                NCF: B0100045892
                Arroz Selecto 5 lb - RD$ 225.00
                Habichuelas rojas 2 lb - RD$ 170.00
                Aceite Crisol 1 l - RD$ 195.00
                Pollo fresco 3 lb - RD$ 270.00
                Plátanos verdes 6 ud - RD$ 150.00
                Huevos criollos 12 ud - RD$ 120.00
                Pañales Huggies Talla 3 32 ud - RD$ 490.00
                Leche entera Rica 1 l - RD$ 85.00
                Total RD$ 1705.00
            """.trimIndent()

            val parsed = ReceiptTextParser.parseDetailed(sampleReceiptTicket)
            // Open review dialog for human inspection (never write silently to DB)
            pendingReceiptResult = parsed
        }
    }

    // Back handling for sub-views
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        viewModel.selectTab(ScreenTab.HOME)
    }

    val navItems = listOf(
        NavItemData(ScreenTab.HOME, "Hoy", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
        NavItemData(ScreenTab.PANTRY, "Despensa", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_tab_pantry"),
        NavItemData(ScreenTab.COOKING, "Cocina", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_tab_cooking"),
        NavItemData(ScreenTab.SHOPPING, "Lista", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_tab_shopping"),
        NavItemData(ScreenTab.HOUSEHOLD, "Hogar", Icons.Filled.Group, Icons.Outlined.Group, "nav_tab_household")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCaptureSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
                    .testTag("fab_capture")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Capturar productos"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            NavBar3D(
                items = navItems.map { item ->
                    val isSelected = when (item.tab) {
                        ScreenTab.HOME -> currentTab == ScreenTab.HOME
                        ScreenTab.PANTRY -> currentTab == ScreenTab.PANTRY
                        ScreenTab.COOKING -> currentTab == ScreenTab.COOKING || currentTab == ScreenTab.RECIPES || currentTab == ScreenTab.CHEF_IDEAS
                        ScreenTab.SHOPPING -> currentTab == ScreenTab.SHOPPING || currentTab == ScreenTab.LIST
                        ScreenTab.HOUSEHOLD -> currentTab == ScreenTab.HOUSEHOLD || currentTab == ScreenTab.SETTINGS || currentTab == ScreenTab.BABY
                        else -> currentTab == item.tab
                    }
                    NavDockItem(
                        label = item.label,
                        selectedIcon = item.selectedIcon,
                        unselectedIcon = item.unselectedIcon,
                        isSelected = isSelected,
                        testTag = item.testTag,
                        onClick = { viewModel.selectTab(item.tab) }
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    ScreenTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenScan = { showScanDialog = true },
                        onOpenManualAdd = {
                            editingProduct = null
                            activeReminderIdForForm = null
                            showFormDialog = true
                        },
                        onEditItem = { item ->
                            editingProduct = item
                            activeReminderIdForForm = null
                            showFormDialog = true
                        },
                        onOpenPaywall = { viewModel.openPaywall() }
                    )

                    ScreenTab.PANTRY -> PantryScreen(
                        viewModel = viewModel,
                        onEditItem = { item ->
                            editingProduct = item
                            activeReminderIdForForm = null
                            showFormDialog = true
                        },
                        onOpenManualAdd = {
                            editingProduct = null
                            activeReminderIdForForm = null
                            showFormDialog = true
                        },
                        onOpenCaptureSheet = { showCaptureSheet = true },
                        onOpenPaywall = { viewModel.openPaywall() }
                    )

                    ScreenTab.COOKING, ScreenTab.RECIPES, ScreenTab.CHEF_IDEAS -> CookingScreen(
                        viewModel = viewModel,
                        onOpenColmadoPrices = { showPricesDialog = true },
                        onOpenPaywall = { viewModel.openPaywall() }
                    )

                    ScreenTab.SHOPPING, ScreenTab.LIST -> ShoppingListScreen(
                        viewModel = viewModel,
                        onMoveReminderToPantry = { preset, reminderId ->
                            editingProduct = preset
                            activeReminderIdForForm = reminderId
                            showFormDialog = true
                        },
                        onOpenPaywall = { viewModel.openPaywall() }
                    )

                    ScreenTab.HOUSEHOLD, ScreenTab.SETTINGS, ScreenTab.BABY -> HouseholdScreen(
                        viewModel = viewModel,
                        onOpenColmadoPrices = { showPricesDialog = true },
                        onOpenPaywall = { viewModel.openPaywall() }
                    )

                    ScreenTab.ADD -> {
                        // Redirect to pantry and open capture sheet
                        LaunchedEffect(Unit) {
                            viewModel.selectTab(ScreenTab.PANTRY)
                            showCaptureSheet = true
                        }
                        PantryScreen(
                            viewModel = viewModel,
                            onEditItem = { item ->
                                editingProduct = item
                                activeReminderIdForForm = null
                                showFormDialog = true
                            },
                            onOpenManualAdd = {
                                editingProduct = null
                                activeReminderIdForForm = null
                                showFormDialog = true
                            },
                            onOpenCaptureSheet = { showCaptureSheet = true },
                            onOpenPaywall = { viewModel.openPaywall() }
                        )
                    }
                }
            }

            // Celebration Burst animation
            CelebrationBurst(trigger = burstTimestamp)
        }
    }

    // Capture Bottom Sheet (M2 Entry Points)
    if (showCaptureSheet) {
        CaptureBottomSheet(
            onDismiss = { showCaptureSheet = false },
            onOpenReceiptPhoto = {
                if (viewModel.canScanReceipt()) {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            },
            onOpenBarcodeScan = { showScanDialog = true },
            onOpenPasteText = { showPasteDialog = true },
            onOpenManualAdd = {
                editingProduct = null
                activeReminderIdForForm = null
                showFormDialog = true
            }
        )
    }

    // Human-in-the-Loop Receipt Review Dialog (ADR-05)
    pendingReceiptResult?.let { result ->
        ReceiptReviewDialog(
            initialResult = result,
            onDismiss = { pendingReceiptResult = null },
            onConfirm = { storeName, lines, totalExpense ->
                val success = viewModel.confirmReceiptCapture(storeName, lines, totalExpense)
                if (success) {
                    viewModel.recordReceiptScan()
                    pendingReceiptResult = null
                    Toast.makeText(
                        context,
                        "Factura procesada: ${lines.size} productos agregados",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.selectTab(ScreenTab.PANTRY)
                }
            }
        )
    }

    // Paywall Modal Dialog (Google Play Billing + RD$1,690 Lifetime Offer)
    if (showPaywall) {
        PaywallDialog(
            onDismiss = { viewModel.closePaywall() },
            onBuy = { act -> viewModel.buyPro(act) },
            onRestore = { viewModel.restorePurchases() },
            onUnlockWithCode = { code -> viewModel.unlockProWithCode(code) }
        )
    }

    // Exact Limit Alert Dialog (30 products, 5 receipt scans, reports, multi-user)
    activeLimitDialog?.let { limitType ->
        LimitAlertDialog(
            limitType = limitType,
            onDismiss = { viewModel.dismissLimitDialog() },
            onOpenPaywall = {
                viewModel.dismissLimitDialog()
                viewModel.openPaywall()
            }
        )
    }

    // Product Form Dialog (Location, Brand, Expiry, Profile)
    if (showFormDialog) {
        ProductFormDialog(
            initialItem = editingProduct,
            fromReminderId = activeReminderIdForForm,
            onDismiss = {
                showFormDialog = false
                editingProduct = null
                activeReminderIdForForm = null
            },
            onSave = { item, reminderId ->
                val saved = viewModel.saveItem(item, reminderId)
                if (saved) {
                    showFormDialog = false
                    editingProduct = null
                    activeReminderIdForForm = null
                    Toast.makeText(context, "Guardado en despensa", Toast.LENGTH_SHORT).show()
                }
            },
            onSaveAndAddAnother = { item, reminderId ->
                val saved = viewModel.saveItem(item, reminderId)
                if (saved) {
                    editingProduct = null
                    activeReminderIdForForm = null
                    Toast.makeText(context, "Guardado. Añade el siguiente producto", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = { id ->
                viewModel.deleteItem(id)
                showFormDialog = false
                editingProduct = null
                activeReminderIdForForm = null
                Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Live CameraX Barcode Scanner Dialog
    if (showScanDialog) {
        BarcodeScannerDialog(
            existingPantryItems = rawItems,
            onDismiss = { showScanDialog = false },
            onBarcodeScanned = { item ->
                showScanDialog = false
                editingProduct = item
                activeReminderIdForForm = null
                showFormDialog = true
                Toast.makeText(
                    context,
                    if (item.name.isNotBlank()) "Producto reconocido" else "Código registrado, completa el nombre",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Pasted Text / Dictado Dialog
    if (showPasteDialog) {
        PastedTextDialog(
            onDismiss = { showPasteDialog = false },
            onAddItems = { items, source ->
                val ok = viewModel.addItemsFromBatch(items, source)
                if (ok) {
                    Toast.makeText(context, "${items.size} productos añadidos a tu despensa", Toast.LENGTH_SHORT).show()
                    viewModel.selectTab(ScreenTab.PANTRY)
                }
            }
        )
    }

    // Colmado Prices Dialog
    if (showPricesDialog) {
        ColmadoPricesDialog(
            customPrices = config.customPrices,
            onDismiss = { showPricesDialog = false },
            onSavePrices = { map ->
                map.forEach { (k, v) -> viewModel.setCustomPrice(k, v) }
                Toast.makeText(context, "Precios del colmado actualizados", Toast.LENGTH_SHORT).show()
            },
            onReset = {
                viewModel.resetPrices()
                Toast.makeText(context, "Precios restablecidos a los de referencia", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
