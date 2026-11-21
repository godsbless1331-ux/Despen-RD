package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.billing.BillingManager
import com.example.data.local.AppColorTheme
import com.example.data.local.AppConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ExpenseEntity
import com.example.data.local.MealPlanEntryEntity
import com.example.data.local.MonetizationConstants
import com.example.data.local.PantryDao
import com.example.data.local.PantryEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.ReminderEntity
import com.example.data.local.ShoppingListItemEntity
import com.example.data.local.SortOption
import com.example.data.local.StockMovementEntity
import com.example.data.local.ThemeModeOption
import com.example.data.model.BabySubCategory
import com.example.data.model.Category
import com.example.data.model.ChefIdea
import com.example.data.model.ChefIdeasKnowledgeBase
import com.example.data.model.HouseholdExpense
import com.example.data.model.Location
import com.example.data.model.MealPlanEntry
import com.example.data.model.MealSlot
import com.example.data.model.MovementType
import com.example.data.model.PantryItem
import com.example.data.model.ParsedReceiptLine
import com.example.data.model.ProfileKind
import com.example.data.model.Recipe
import com.example.data.model.RecipeMealType
import com.example.data.model.ShoppingReminder
import com.example.data.model.StockMovement
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

enum class ScreenTab {
    HOME,       // Hoy (agenda, alertas accionables, resumen)
    PANTRY,     // Despensa (inventario con ubicaciones y filtros)
    COOKING,    // Cocina (recetas criollas, chef IA, plan semanal)
    SHOPPING,   // Lista (lista unificada, presupuesto, modo compra)
    HOUSEHOLD,  // Hogar (perfiles reales, finanzas, respaldo simétrico, ajustes)
    // Aliases for backwards compatibility:
    BABY,
    ADD,
    LIST,
    SETTINGS,
    RECIPES,
    CHEF_IDEAS
}

enum class LimitType(val title: String, val exactMessage: String) {
    PRODUCT_LIMIT(
        title = "Límite de productos alcanzado",
        exactMessage = "Has alcanzado el límite de 30 productos en la versión gratuita. Desbloquea Despensa Pro para añadir productos ilimitados."
    ),
    RECEIPT_SCAN_LIMIT(
        title = "Límite de escaneos alcanzado",
        exactMessage = "Has alcanzado el límite de 5 escaneos de facturas este mes en la versión gratuita. Desbloquea Despensa Pro para escanear facturas sin límites."
    ),
    REPORT_HISTORY_LIMIT(
        title = "Historial limitado en versión gratuita",
        exactMessage = "Los reportes e historial de días anteriores están disponibles exclusivamente en Despensa Pro. Desbloquea Pro para ver el historial completo y análisis de todos los meses."
    ),
    PROFILE_LIMIT(
        title = "Modo multi-usuario Pro",
        exactMessage = "El modo multi-usuario y perfiles adicionales están disponibles exclusivamente en Despensa Pro. Desbloquea Pro para gestionar múltiples despensas y usuarios."
    )
}

class PantryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val pantryDao: PantryDao = database.pantryDao()
    private val movementDao = database.movementDao()
    private val shoppingDao = database.shoppingDao()
    private val expenseDao = database.expenseDao()
    private val mealPlanDao = database.mealPlanDao()
    private val reminderDao = database.reminderDao()

    val prefs = PreferencesManager(application)
    val billingManager = BillingManager(application, prefs, viewModelScope)

    val config: StateFlow<AppConfig> = prefs.configFlow

    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedLocation = MutableStateFlow("all")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _selectedBabySub = MutableStateFlow("all")
    val selectedBabySub: StateFlow<String> = _selectedBabySub.asStateFlow()

    private val _recipeFilter = MutableStateFlow("all")
    val recipeFilter: StateFlow<String> = _recipeFilter.asStateFlow()

    private val _recipeMealFilter = MutableStateFlow<RecipeMealType?>(null)
    val recipeMealFilter: StateFlow<RecipeMealType?> = _recipeMealFilter.asStateFlow()

    private val _recipeServings = MutableStateFlow(4)
    val recipeServings: StateFlow<Int> = _recipeServings.asStateFlow()

    // Chef ideas state
    private val _selectedProductsForChef = MutableStateFlow<Set<String>>(emptySet())
    val selectedProductsForChef: StateFlow<Set<String>> = _selectedProductsForChef.asStateFlow()

    private val _chefMealFilter = MutableStateFlow<RecipeMealType?>(null)
    val chefMealFilter: StateFlow<RecipeMealType?> = _chefMealFilter.asStateFlow()

    private val _generatedChefIdeas = MutableStateFlow<List<ChefIdea>>(emptyList())
    val generatedChefIdeas: StateFlow<List<ChefIdea>> = _generatedChefIdeas.asStateFlow()

    // Paywall and Limit dialog triggers
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    private val _activeLimitDialog = MutableStateFlow<LimitType?>(null)
    val activeLimitDialog: StateFlow<LimitType?> = _activeLimitDialog.asStateFlow()

    private val _burstTrigger = MutableSharedFlow<Long>()
    val burstTrigger: SharedFlow<Long> = _burstTrigger.asSharedFlow()

    // Raw pantry items from Room
    val rawPantryItems: StateFlow<List<PantryItem>> = pantryDao.getAllItems()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Movements ledger from Room
    val movements: StateFlow<List<StockMovement>> = movementDao.getAllMovements()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified Shopping list items from Room
    val shoppingItems: StateFlow<List<ShoppingListItemEntity>> = shoppingDao.getAllShoppingItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Household expenses from Room
    val expenses: StateFlow<List<HouseholdExpense>> = expenseDao.getAllExpenses()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly Meal plan entries from Room
    val mealPlanEntries: StateFlow<List<MealPlanEntry>> = mealPlanDao.getAllEntries()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders
    val reminders: StateFlow<List<ShoppingReminder>> = reminderDao.getAllReminders()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Diaper calculation
    val babyDiaperDaysRemaining: StateFlow<Int> = rawPantryItems.combine(config) { items, cfg ->
        val activeProfile = cfg.profiles.find { it.id == cfg.currentProfileId }
        val dailyUsage = activeProfile?.diapersPerDay ?: cfg.diapersPerDay
        val totalDiapers = items
            .filter { it.cat == Category.BEBE && it.sub == "panales" }
            .sumOf { item ->
                // If counted in packages, convert to units (approx 32 units/pack)
                if (item.unit == "paq" || item.unit == "caja") item.qty * 32.0 else item.qty
            }
        if (dailyUsage > 0) (totalDiapers / dailyUsage).toInt() else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current month expenses sum
    val currentMonthExpensesTotal: StateFlow<Double> = expenses.map { list ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfMonth = cal.timeInMillis
        list.filter { it.timestamp >= startOfMonth }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private data class FilterState(
        val query: String,
        val catId: String,
        val locId: String,
        val babySub: String
    )

    private val _filterState = combine(
        _searchQuery,
        _selectedCategory,
        _selectedLocation,
        _selectedBabySub
    ) { query, catId, locId, babySub ->
        FilterState(query, catId, locId, babySub)
    }

    // Filtered pantry items
    val filteredPantryItems: StateFlow<List<PantryItem>> = combine(
        rawPantryItems,
        _filterState,
        config
    ) { items, filter, cfg ->
        var res = items

        // Filter by active profile if set and not "all" (Pro multi-profile)
        if (cfg.currentProfileId != "all") {
            // Include items for active profile or unassigned
            res = res.filter { it.profileId == cfg.currentProfileId || it.profileId == "p_main" }
        }

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            res = res.filter {
                it.name.lowercase().contains(q) ||
                        it.brand.lowercase().contains(q) ||
                        it.barcode.contains(q) ||
                        it.cat.displayName.lowercase().contains(q)
            }
        }

        if (filter.catId != "all") {
            res = res.filter { it.cat.id == filter.catId }
        }

        if (filter.locId != "all") {
            res = res.filter { it.location.id == filter.locId }
        }

        if (filter.catId == Category.BEBE.id && filter.babySub != "all") {
            res = res.filter { it.sub == filter.babySub }
        }

        when (cfg.sortOption) {
            SortOption.NAME -> res.sortedBy { it.name.lowercase() }
            SortOption.EXP -> res.sortedWith(compareBy<PantryItem> { it.expDate.isBlank() }.thenBy { it.expDate })
            SortOption.QTY -> res.sortedBy { it.qty }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History limited to today in free version
    val history: StateFlow<List<StockMovement>> = movements.combine(config) { list, cfg ->
        if (cfg.isProUser) {
            list
        } else {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = cal.timeInMillis
            list.filter { it.timestamp >= startOfToday }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPastHistoryCount: StateFlow<Int> = movements.combine(history) { raw, filtered ->
        Math.max(0, raw.size - filtered.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Navigation
    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(catId: String) {
        _selectedCategory.value = catId
    }

    fun setLocationFilter(locId: String) {
        _selectedLocation.value = locId
    }

    fun setBabySubFilter(subId: String) {
        _selectedBabySub.value = subId
    }

    fun setRecipeFilter(filter: String) {
        _recipeFilter.value = filter
    }

    fun setRecipeMealFilter(mealType: RecipeMealType?) {
        _recipeMealFilter.value = mealType
    }

    fun openCookingForMeal(mealType: RecipeMealType?) {
        _recipeMealFilter.value = mealType
        _chefMealFilter.value = mealType
        _currentTab.value = ScreenTab.COOKING
        generateChefIdeas()
    }

    fun setChefMealFilter(mealType: RecipeMealType?) {
        _chefMealFilter.value = mealType
        generateChefIdeas()
    }

    fun setRecipeServings(servings: Int) {
        _recipeServings.value = servings
    }

    fun openPaywall() {
        _showPaywall.value = true
    }

    fun closePaywall() {
        _showPaywall.value = false
    }

    fun showLimitDialog(type: LimitType) {
        _activeLimitDialog.value = type
    }

    fun dismissLimitDialog() {
        _activeLimitDialog.value = null
    }

    fun triggerBurst() {
        viewModelScope.launch {
            _burstTrigger.emit(System.currentTimeMillis())
        }
    }

    // Limit check for products
    fun canAddProduct(isEditingExisting: Boolean): Boolean {
        if (config.value.isProUser || isEditingExisting) return true
        val currentCount = rawPantryItems.value.size
        return if (currentCount < MonetizationConstants.FREE_PRODUCT_LIMIT) {
            true
        } else {
            showLimitDialog(LimitType.PRODUCT_LIMIT)
            false
        }
    }

    // Limit check for receipt scans
    fun canScanReceipt(): Boolean {
        if (config.value.isProUser) return true
        val currentScans = config.value.monthlyReceiptScansCount
        return if (currentScans < MonetizationConstants.FREE_MONTHLY_RECEIPT_SCANS_LIMIT) {
            true
        } else {
            showLimitDialog(LimitType.RECEIPT_SCAN_LIMIT)
            false
        }
    }

    // Save product with movement ledger logging
    fun saveItem(item: PantryItem, fromReminderId: String? = null): Boolean {
        val existing = rawPantryItems.value.find {
            it.id == item.id || (it.name.equals(item.name, ignoreCase = true) && it.unit == item.unit)
        }
        val isNew = existing == null

        if (isNew && !canAddProduct(isEditingExisting = false)) {
            return false
        }

        viewModelScope.launch {
            val delta = if (existing != null) item.qty - existing.qty else item.qty
            val movementType = if (isNew) MovementType.IN else if (delta > 0) MovementType.IN else MovementType.ADJUST

            val finalItem = item.copy(
                profileId = if (item.profileId.isBlank()) config.value.currentProfileId else item.profileId
            )
            pantryDao.insertItem(PantryEntity.fromDomain(finalItem))

            // Record in ledger
            movementDao.insertMovement(
                StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = finalItem.id,
                    itemName = finalItem.name,
                    profileId = finalItem.profileId,
                    type = movementType.name,
                    delta = Math.abs(delta),
                    unit = finalItem.unit,
                    reason = if (isNew) "Ingreso manual" else "Actualización de producto",
                    source = "MANUAL",
                    timestamp = System.currentTimeMillis(),
                    cost = 0.0,
                    idempotencyKey = UUID.randomUUID().toString()
                )
            )

            if (!fromReminderId.isNullOrBlank()) {
                reminderDao.deleteById(fromReminderId)
            }
            triggerBurst()
        }
        return true
    }

    // Consume product (decrements stock & registers OUT in ledger)
    fun decrementQuantity(itemId: String, reason: String = "Consumo") {
        viewModelScope.launch {
            val item = rawPantryItems.value.find { it.id == itemId } ?: return@launch
            val step = when (item.unit) {
                "g", "ml" -> 100.0
                "ud", "paq", "lata", "caja" -> 1.0
                else -> 0.5
            }
            val newQty = Math.max(0.0, Math.round((item.qty - step) * 100.0) / 100.0)

            if (newQty <= 0.0 && item.minQty <= 0) {
                pantryDao.deleteById(item.id)
            } else {
                pantryDao.updateItem(PantryEntity.fromDomain(item.copy(qty = newQty)))
            }

            // Log OUT movement
            movementDao.insertMovement(
                StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = item.id,
                    itemName = item.name,
                    profileId = item.profileId,
                    type = MovementType.OUT.name,
                    delta = step,
                    unit = item.unit,
                    reason = reason,
                    source = "MANUAL",
                    timestamp = System.currentTimeMillis(),
                    cost = 0.0,
                    idempotencyKey = UUID.randomUUID().toString()
                )
            )
        }
    }

    // Register Waste / Spoilage (Merma)
    fun recordWaste(itemId: String, amount: Double = 1.0, reason: String = "Vencimiento / Dañado") {
        viewModelScope.launch {
            val item = rawPantryItems.value.find { it.id == itemId } ?: return@launch
            val wasteDelta = Math.min(item.qty, amount)
            val newQty = Math.max(0.0, item.qty - wasteDelta)

            if (newQty <= 0.0 && item.minQty <= 0) {
                pantryDao.deleteById(item.id)
            } else {
                pantryDao.updateItem(PantryEntity.fromDomain(item.copy(qty = newQty)))
            }

            movementDao.insertMovement(
                StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = item.id,
                    itemName = item.name,
                    profileId = item.profileId,
                    type = MovementType.WASTE.name,
                    delta = wasteDelta,
                    unit = item.unit,
                    reason = reason,
                    source = "MANUAL",
                    timestamp = System.currentTimeMillis(),
                    cost = 0.0,
                    idempotencyKey = UUID.randomUUID().toString()
                )
            )
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            pantryDao.deleteById(id)
        }
    }

    // Batch receipt confirmation with review
    fun confirmReceiptCapture(storeName: String, lines: List<ParsedReceiptLine>, totalExpense: Double): Boolean {
        val currentCount = rawPantryItems.value.size
        if (!config.value.isProUser && currentCount + lines.size > MonetizationConstants.FREE_PRODUCT_LIMIT) {
            showLimitDialog(LimitType.PRODUCT_LIMIT)
            return false
        }

        viewModelScope.launch {
            var addedCount = 0
            val activeProfileId = config.value.currentProfileId

            for (line in lines) {
                val existing = rawPantryItems.value.find {
                    it.name.equals(line.name, ignoreCase = true) && it.unit == line.unit
                }

                if (existing != null) {
                    val updated = existing.copy(qty = existing.qty + line.qty)
                    pantryDao.updateItem(PantryEntity.fromDomain(updated))
                } else {
                    val newItem = PantryItem(
                        name = line.name,
                        qty = line.qty,
                        unit = line.unit,
                        cat = line.category,
                        profileId = activeProfileId,
                        sub = line.sub
                    )
                    pantryDao.insertItem(PantryEntity.fromDomain(newItem))
                }

                // Log movement IN
                movementDao.insertMovement(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = existing?.id ?: UUID.randomUUID().toString(),
                        itemName = line.name,
                        profileId = activeProfileId,
                        type = MovementType.IN.name,
                        delta = line.qty,
                        unit = line.unit,
                        reason = "Compra en $storeName",
                        source = "RECEIPT",
                        timestamp = System.currentTimeMillis(),
                        cost = line.unitPrice,
                        idempotencyKey = UUID.randomUUID().toString()
                    )
                )

                // Update custom price observation
                if (line.unitPrice > 0.0) {
                    prefs.setCustomPrice(line.name.lowercase(), line.unitPrice / line.qty)
                }
                addedCount++
            }

            // Register Expense
            expenseDao.insertExpense(
                ExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    storeName = storeName,
                    amount = if (totalExpense > 0.0) totalExpense else lines.sumOf { it.unitPrice },
                    itemCount = addedCount,
                    profileId = activeProfileId,
                    timestamp = System.currentTimeMillis(),
                    source = "RECEIPT"
                )
            )

            prefs.incrementReceiptScanCount()
            triggerBurst()
        }
        return true
    }

    fun addItemsFromBatch(items: List<PantryItem>, sourceName: String): Boolean {
        val currentCount = rawPantryItems.value.size
        if (!config.value.isProUser && currentCount + items.size > MonetizationConstants.FREE_PRODUCT_LIMIT) {
            showLimitDialog(LimitType.PRODUCT_LIMIT)
            return false
        }

        viewModelScope.launch {
            var addedCount = 0
            val activeProfileId = config.value.currentProfileId

            for (item in items) {
                val existing = rawPantryItems.value.find {
                    it.name.equals(item.name, ignoreCase = true) && it.unit == item.unit
                }
                if (existing != null) {
                    val updated = existing.copy(qty = existing.qty + item.qty)
                    pantryDao.updateItem(PantryEntity.fromDomain(updated))
                } else {
                    pantryDao.insertItem(PantryEntity.fromDomain(item.copy(profileId = activeProfileId)))
                }

                movementDao.insertMovement(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = existing?.id ?: UUID.randomUUID().toString(),
                        itemName = item.name,
                        profileId = activeProfileId,
                        type = MovementType.IN.name,
                        delta = item.qty,
                        unit = item.unit,
                        reason = "Ingreso por $sourceName",
                        source = "BATCH",
                        timestamp = System.currentTimeMillis(),
                        cost = 0.0,
                        idempotencyKey = UUID.randomUUID().toString()
                    )
                )
                addedCount++
            }
            triggerBurst()
        }
        return true
    }

    fun recordReceiptScan() {
        prefs.incrementReceiptScanCount()
    }

    // Unified Shopping List Management
    fun toggleShoppingItem(id: String, isChecked: Boolean) {
        viewModelScope.launch {
            shoppingDao.setChecked(id, isChecked)
        }
    }

    fun addShoppingItem(name: String, qty: Double = 1.0, unit: String = "ud", estPrice: Double = 0.0, source: String = "MANUAL") {
        if (name.isBlank()) return
        viewModelScope.launch {
            shoppingDao.insertItem(
                ShoppingListItemEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    qty = if (qty > 0) qty else 1.0,
                    unit = unit,
                    estPrice = estPrice,
                    isChecked = false,
                    profileId = config.value.currentProfileId,
                    source = source
                )
            )
        }
    }

    fun deleteShoppingItem(id: String) {
        viewModelScope.launch {
            shoppingDao.deleteById(id)
        }
    }

    fun clearCheckedShoppingItems() {
        viewModelScope.launch {
            shoppingDao.deleteChecked()
        }
    }

    // Finish Shopping (Modo Colmado / Conciliación):
    // Moves checked items to pantry with IN movements and records expense!
    fun checkoutShoppingList(storeName: String = "Colmado Dominicano"): Int {
        val checked = shoppingItems.value.filter { it.isChecked }
        if (checked.isEmpty()) return 0

        viewModelScope.launch {
            var totalExpense = 0.0
            val activeProfileId = config.value.currentProfileId

            for (item in checked) {
                val existing = rawPantryItems.value.find {
                    it.name.equals(item.name, ignoreCase = true) && it.unit == item.unit
                }
                if (existing != null) {
                    pantryDao.updateItem(PantryEntity.fromDomain(existing.copy(qty = existing.qty + item.qty)))
                } else {
                    pantryDao.insertItem(
                        PantryEntity(
                            id = UUID.randomUUID().toString(),
                            name = item.name,
                            qty = item.qty,
                            unit = item.unit,
                            catId = Category.ALIMENTO.id,
                            locationId = "despensa",
                            profileId = activeProfileId,
                            minQty = 1.0,
                            expDate = "",
                            barcode = "",
                            sub = ""
                        )
                    )
                }

                movementDao.insertMovement(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = existing?.id ?: UUID.randomUUID().toString(),
                        itemName = item.name,
                        profileId = activeProfileId,
                        type = MovementType.IN.name,
                        delta = item.qty,
                        unit = item.unit,
                        reason = "Compra en $storeName",
                        source = "SHOPPING_CHECKOUT",
                        timestamp = System.currentTimeMillis(),
                        cost = item.estPrice,
                        idempotencyKey = UUID.randomUUID().toString()
                    )
                )
                totalExpense += item.estPrice
            }

            // Register Expense
            expenseDao.insertExpense(
                ExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    storeName = storeName,
                    amount = totalExpense,
                    itemCount = checked.size,
                    profileId = activeProfileId,
                    timestamp = System.currentTimeMillis(),
                    source = "SHOPPING_LIST"
                )
            )

            shoppingDao.deleteChecked()
            triggerBurst()
        }
        return checked.size
    }

    // Add Missing Recipe Ingredients to unified Shopping List
    fun addMissingRecipeIngredients(recipeId: Int): Int {
        val recipe = Recipe.ALL.find { it.id == recipeId } ?: return 0
        val pantry = rawPantryItems.value
        var added = 0

        viewModelScope.launch {
            recipe.ingredientPatterns.forEach { pattern ->
                if (!recipe.hasIngredient(pattern, pantry)) {
                    val cleanName = pattern.split("|")[0].replaceFirstChar { it.uppercase() }
                    val alreadyInShopping = shoppingItems.value.any { !it.isChecked && it.name.equals(cleanName, ignoreCase = true) }
                    if (!alreadyInShopping) {
                        shoppingDao.insertItem(
                            ShoppingListItemEntity(
                                id = UUID.randomUUID().toString(),
                                name = cleanName,
                                qty = 1.0,
                                unit = "ud",
                                estPrice = 65.0,
                                isChecked = false,
                                profileId = config.value.currentProfileId,
                                source = "RECIPE"
                            )
                        )
                        added++
                    }
                }
            }
        }
        return added
    }

    // Send Weekly Meal Plan Missings to Shopping List
    fun sendMealPlanMissingsToShoppingList(): Int {
        val plan = mealPlanEntries.value.filter { !it.isCooked }
        val pantry = rawPantryItems.value
        var added = 0

        viewModelScope.launch {
            for (entry in plan) {
                val recipe = Recipe.ALL.find { it.id == entry.recipeId } ?: continue
                recipe.ingredientPatterns.forEach { pattern ->
                    if (!recipe.hasIngredient(pattern, pantry)) {
                        val cleanName = pattern.split("|")[0].replaceFirstChar { it.uppercase() }
                        val alreadyInShopping = shoppingItems.value.any { !it.isChecked && it.name.equals(cleanName, ignoreCase = true) }
                        if (!alreadyInShopping) {
                            shoppingDao.insertItem(
                                ShoppingListItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = cleanName,
                                    qty = 1.0,
                                    unit = "ud",
                                    estPrice = 75.0,
                                    isChecked = false,
                                    profileId = config.value.currentProfileId,
                                    source = "MEAL_PLAN"
                                )
                            )
                            added++
                        }
                    }
                }
            }
        }
        return added
    }

    fun toggleMealPlanCooked(entryId: String, isCooked: Boolean) {
        viewModelScope.launch {
            mealPlanDao.setCooked(entryId, isCooked)
        }
    }

    fun setMealPlanEntry(dayOfWeek: Int, slot: MealSlot, recipe: Recipe) {
        viewModelScope.launch {
            mealPlanDao.insertOrUpdate(
                MealPlanEntryEntity(
                    id = "meal_${dayOfWeek}_${slot.name}",
                    dayOfWeek = dayOfWeek,
                    slot = slot.name,
                    recipeId = recipe.id,
                    recipeName = recipe.name,
                    servings = 4,
                    isCooked = false
                )
            )
        }
    }

    // Reminders (legacy compatibility)
    fun addReminder(name: String, dueDate: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            reminderDao.insertReminder(
                ReminderEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    dueDate = dueDate,
                    isDone = false,
                    createdAt = System.currentTimeMillis()
                )
            )
            // Also keep unified shopping list in sync
            addShoppingItem(name = name, qty = 1.0, unit = "ud", source = "MANUAL")
        }
    }

    fun toggleReminder(id: String) {
        viewModelScope.launch {
            val reminder = reminders.value.find { it.id == id } ?: return@launch
            val updated = reminder.copy(isDone = !reminder.isDone)
            reminderDao.updateReminder(ReminderEntity.fromDomain(updated))
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderDao.deleteById(id)
        }
    }

    fun clearCompletedReminders() {
        viewModelScope.launch {
            reminderDao.clearCompleted()
            shoppingDao.deleteChecked()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            movementDao.clearAll()
        }
    }

    // Chef Ideas Feature
    fun toggleProductForChef(productName: String) {
        val current = _selectedProductsForChef.value.toMutableSet()
        if (current.contains(productName)) {
            current.remove(productName)
        } else {
            current.add(productName)
        }
        _selectedProductsForChef.value = current
        generateChefIdeas()
    }

    fun selectAllFoodProductsForChef() {
        val foods = rawPantryItems.value
            .filter { it.cat == Category.ALIMENTO && it.qty > 0 }
            .map { it.name }
            .toSet()
        _selectedProductsForChef.value = foods
        generateChefIdeas()
    }

    fun clearChefSelectedProducts() {
        _selectedProductsForChef.value = emptySet()
        _generatedChefIdeas.value = emptyList()
    }

    fun generateChefIdeas() {
        val list = _selectedProductsForChef.value.toList()
        val mealFilter = _chefMealFilter.value
        if (list.isEmpty()) {
            val allFoods = rawPantryItems.value
                .filter { it.cat == Category.ALIMENTO && it.qty > 0 }
                .map { it.name }
            _generatedChefIdeas.value = ChefIdeasKnowledgeBase.generateIdeasFor(allFoods, mealFilter)
        } else {
            _generatedChefIdeas.value = ChefIdeasKnowledgeBase.generateIdeasFor(list, mealFilter)
        }
    }

    // Multi-profile limit check
    fun trySwitchProfile(profileId: String): Boolean {
        if (config.value.isProUser || profileId == "p_main") {
            prefs.setCurrentProfile(profileId)
            return true
        } else {
            showLimitDialog(LimitType.PROFILE_LIMIT)
            return false
        }
    }

    fun tryAddProfile(name: String, emoji: String, kind: ProfileKind = ProfileKind.ADULT): Boolean {
        if (config.value.isProUser) {
            val newProfile = UserProfile(
                id = "p_${UUID.randomUUID().toString().take(6)}",
                name = name,
                emoji = emoji,
                kind = kind
            )
            prefs.addProfile(newProfile)
            prefs.setCurrentProfile(newProfile.id)
            return true
        } else {
            showLimitDialog(LimitType.PROFILE_LIMIT)
            return false
        }
    }

    // Google Play Billing Actions
    fun buyPro(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    // Unlock Pro with Secret VIP Code / Password
    fun unlockProWithCode(enteredCode: String): Boolean {
        val clean = enteredCode.trim().uppercase().replace(" ", "").replace("-", "")
        val validCodes = setOf(
            "RD2026",
            "PRO26",
            "VIP26",
            "GEO26",
            "MANAGEMENTTECHGEO"
        )
        if (clean in validCodes) {
            prefs.setProUser(
                isPro = true,
                purchaseToken = "VIP_PASS_${clean}_${System.currentTimeMillis()}"
            )
            triggerBurst()
            return true
        }
        return false
    }

    // Preferences Setters
    fun setColorTheme(theme: AppColorTheme) = prefs.setColorTheme(theme)
    fun setThemeMode(mode: ThemeModeOption) = prefs.setThemeMode(mode)
    fun setPantryName(name: String) = prefs.setPantryName(name)
    fun setWarningDays(days: Int) = prefs.setWarningDays(days)
    fun setSortOption(sort: SortOption) = prefs.setSortOption(sort)
    fun setCustomPrice(key: String, price: Double) = prefs.setCustomPrice(key, price)
    fun resetPrices() = prefs.resetPrices()
    fun setMonthlyBudget(budget: Double) = prefs.setMonthlyBudget(budget)
    fun setDiapersPerDay(count: Int) = prefs.setDiapersPerDay(count)
    fun setVibrateOnScan(vibrate: Boolean) = prefs.setVibrateOnScan(vibrate)

    // Symmetric Backup: Export and Import
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())
        root.put("pantryName", config.value.pantryName)
        root.put("budgetRD", config.value.monthlyBudgetRD)

        // 1. Inventory Items
        val itemsArr = JSONArray()
        rawPantryItems.value.forEach { item ->
            itemsArr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("qty", item.qty)
                    put("unit", item.unit)
                    put("catId", item.cat.id)
                    put("locationId", item.location.id)
                    put("profileId", item.profileId)
                    put("minQty", item.minQty)
                    put("expDate", item.expDate)
                    put("barcode", item.barcode)
                    put("sub", item.sub)
                    put("brand", item.brand)
                }
            )
        }
        root.put("inventory", itemsArr)

        // 2. Shopping Items
        val shoppingArr = JSONArray()
        shoppingItems.value.forEach { s ->
            shoppingArr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("qty", s.qty)
                    put("unit", s.unit)
                    put("estPrice", s.estPrice)
                    put("isChecked", s.isChecked)
                    put("profileId", s.profileId)
                    put("source", s.source)
                }
            )
        }
        root.put("shoppingList", shoppingArr)

        // 3. Stock Movements (Ledger)
        val moveArr = JSONArray()
        movements.value.take(100).forEach { m ->
            moveArr.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("itemId", m.itemId)
                    put("itemName", m.itemName)
                    put("profileId", m.profileId)
                    put("type", m.type.name)
                    put("delta", m.delta)
                    put("unit", m.unit)
                    put("reason", m.reason)
                    put("source", m.source)
                    put("timestamp", m.timestamp)
                    put("cost", m.cost)
                }
            )
        }
        root.put("movements", moveArr)

        // 4. Expenses
        val expenseArr = JSONArray()
        expenses.value.take(50).forEach { e ->
            expenseArr.put(
                JSONObject().apply {
                    put("id", e.id)
                    put("storeName", e.storeName)
                    put("amount", e.amount)
                    put("itemCount", e.itemCount)
                    put("profileId", e.profileId)
                    put("timestamp", e.timestamp)
                }
            )
        }
        root.put("expenses", expenseArr)

        // 5. User Profiles
        val profArr = JSONArray()
        config.value.profiles.forEach { p ->
            profArr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("emoji", p.emoji)
                    put("kind", p.kind.name)
                }
            )
        }
        root.put("profiles", profArr)

        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val itemsArr = root.optJSONArray("inventory") ?: root.optJSONArray("items") ?: return false

            viewModelScope.launch {
                val newItems = mutableListOf<PantryEntity>()
                for (i in 0 until itemsArr.length()) {
                    val obj = itemsArr.getJSONObject(i)
                    newItems.add(
                        PantryEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.getString("name"),
                            qty = obj.optDouble("qty", 1.0),
                            unit = obj.optString("unit", "ud"),
                            catId = obj.optString("catId", Category.ALIMENTO.id),
                            locationId = obj.optString("locationId", "despensa"),
                            profileId = obj.optString("profileId", "p_main"),
                            minQty = obj.optDouble("minQty", 0.0),
                            expDate = obj.optString("expDate", ""),
                            barcode = obj.optString("barcode", ""),
                            sub = obj.optString("sub", ""),
                            brand = obj.optString("brand", "")
                        )
                    )
                }
                if (newItems.isNotEmpty()) {
                    pantryDao.clearAll()
                    pantryDao.insertAll(newItems)
                }

                // Restore shopping items if present
                val shoppingArr = root.optJSONArray("shoppingList")
                if (shoppingArr != null && shoppingArr.length() > 0) {
                    val newShopping = mutableListOf<ShoppingListItemEntity>()
                    for (i in 0 until shoppingArr.length()) {
                        val obj = shoppingArr.getJSONObject(i)
                        newShopping.add(
                            ShoppingListItemEntity(
                                id = obj.optString("id", UUID.randomUUID().toString()),
                                name = obj.getString("name"),
                                qty = obj.optDouble("qty", 1.0),
                                unit = obj.optString("unit", "ud"),
                                estPrice = obj.optDouble("estPrice", 0.0),
                                isChecked = obj.optBoolean("isChecked", false),
                                profileId = obj.optString("profileId", "p_main"),
                                source = obj.optString("source", "MANUAL")
                            )
                        )
                    }
                    shoppingDao.clearAll()
                    shoppingDao.insertAll(newShopping)
                }

                // Restore budget
                if (root.has("budgetRD")) {
                    prefs.setMonthlyBudget(root.getDouble("budgetRD"))
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            pantryDao.clearAll()
            movementDao.clearAll()
            shoppingDao.clearAll()
            expenseDao.clearAll()
            mealPlanDao.clearAll()
            reminderDao.clearAll()
            prefs.resetPrices()
        }
    }

    fun generateShoppingListText(): String {
        val lowStock = rawPantryItems.value.filter { it.isLowStock }
        val pendingShopping = shoppingItems.value.filter { !it.isChecked }

        if (lowStock.isEmpty() && pendingShopping.isEmpty()) {
            return ""
        }

        val sb = StringBuilder("🛒 Lista de compras (Despensa RD):\n\n")
        if (lowStock.isNotEmpty()) {
            sb.append("📉 Por reponer en despensa:\n")
            lowStock.forEach {
                sb.append("• ${it.name} (quedan ${it.qty} ${it.unit} en ${it.location.displayName})\n")
            }
            sb.append("\n")
        }

        if (pendingShopping.isNotEmpty()) {
            sb.append("🛍️ Por comprar:\n")
            pendingShopping.forEach {
                val priceStr = if (it.estPrice > 0) " (~RD$ ${String.format(java.util.Locale.US, "%.0f", it.estPrice)})" else ""
                sb.append("• ${it.name} (${it.qty} ${it.unit})$priceStr\n")
            }
        }

        return sb.toString().trim()
    }

    fun exportWebHtml(): String {
        val esc: (String) -> String = { s ->
            s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
        }
        val items = rawPantryItems.value
        val shopping = shoppingItems.value.filter { !it.isChecked }
        val recipes = Recipe.ALL
        val pantryTitle = esc(config.value.pantryName.ifBlank { "Despensa Pro RD" })

        val invRows = items.joinToString("\n") { item ->
            "<tr><td>${item.cat.emoji} ${esc(item.name)}</td><td>${item.qty} ${esc(item.unit)}</td><td>${item.location.emoji} ${esc(item.location.displayName)}</td><td>${esc(item.expDate.ifBlank { "-" })}</td></tr>"
        }

        val shopRows = shopping.joinToString("\n") { s ->
            "<li><strong>${esc(s.name)}</strong> — ${s.qty} ${esc(s.unit)}</li>"
        }

        val recipeCards = recipes.joinToString("\n") { r ->
            val tags = r.mealTypes.joinToString(" ") { "<span class='tag'>${it.emoji} ${esc(it.displayName)}</span>" }
            val cost = r.calculateCost(4, config.value.customPrices, items)
            val ingList = cost.lines.joinToString("") { line ->
                val badge = if (line.haveInPantry) "✅" else "❌"
                "<li>$badge ${esc(line.name)} (${esc(line.quantityDisplay)})</li>"
            }
            val steps = r.instructions.joinToString("") { st -> "<li>${esc(st)}</li>" }
            val mealData = r.mealTypes.joinToString(",") { it.id }
            """
            <div class="card recipe" data-meal="$mealData">
              <div class="rh"><h3>${r.emoji} ${esc(r.name)}</h3><span>⏱️ ${esc(r.time)}</span></div>
              <div class="tags">$tags</div>
              <p><strong>Ingredientes (4 personas):</strong></p><ul>$ingList</ul>
              <p><strong>Cómo se hace:</strong></p><ol>$steps</ol>
            </div>
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <title>$pantryTitle · Sitio Web</title>
              <style>
                body{font-family:system-ui,-apple-system,sans-serif;background:#F5F1E8;color:#1F2B23;margin:0;padding:18px;line-height:1.5}
                .wrap{max-width:900px;margin:0 auto}
                header{background:#23452F;color:#F5F1E8;padding:22px;border-radius:20px;margin-bottom:16px}
                h1,h2,h3{margin:0 0 8px}
                .tabs{display:flex;gap:8px;flex-wrap:wrap;margin:14px 0}
                button{border:1px solid #D6CCB8;background:#FFFCF5;color:#1F2B23;padding:10px 14px;border-radius:12px;font-weight:600;cursor:pointer}
                button.on{background:#23452F;color:#fff;border-color:#23452F}
                .card{background:#FFFCF5;border:1px solid #E4DAC4;border-radius:18px;padding:16px;margin-bottom:12px}
                table{width:100%;border-collapse:collapse}
                th,td{text-align:left;padding:8px;border-bottom:1px solid #E4DAC4}
                .tag{display:inline-block;background:#EDE6D6;color:#23452F;padding:2px 8px;border-radius:99px;font-size:12px;margin-right:4px}
                .rh{display:flex;justify-content:space-between;align-items:center}
              </style>
            </head>
            <body>
              <div class="wrap">
                <header>
                  <small>MANAGEMENT TECH GEO · EDICIÓN WEB</small>
                  <h1>$pantryTitle</h1>
                  <p>Inventario (${items.size} productos), Lista de compras (${shopping.size}) y Recetario Criollo (Desayuno, Almuerzo y Cena).</p>
                </header>
                <div class="tabs">
                  <button class="on" onclick="filterMeal('all',this)">🍽️ Todas</button>
                  <button onclick="filterMeal('desayuno',this)">🌅 Desayuno</button>
                  <button onclick="filterMeal('almuerzo',this)">☀️ Almuerzo</button>
                  <button onclick="filterMeal('cena',this)">🌙 Cena</button>
                </div>
                <section id="recipes">$recipeCards</section>
                <div class="card">
                  <h2>📦 Inventario Actual</h2>
                  <table><thead><tr><th>Producto</th><th>Cantidad</th><th>Ubicación</th><th>Vence</th></tr></thead><tbody>$invRows</tbody></table>
                </div>
                <div class="card">
                  <h2>🛒 Lista de Compras</h2>
                  <ul>${shopRows.ifBlank { "<li>Sin pendientes por comprar</li>" }}</ul>
                </div>
              </div>
              <script>
                function filterMeal(m,btn){
                  document.querySelectorAll('.tabs button').forEach(b=>b.classList.remove('on'));
                  btn.classList.add('on');
                  document.querySelectorAll('.recipe').forEach(el=>{
                    el.style.display=(m==='all'||el.dataset.meal.includes(m))?'block':'none';
                  });
                }
              </script>
            </body>
            </html>
        """.trimIndent()
    }
}

