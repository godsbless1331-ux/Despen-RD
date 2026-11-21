package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Category
import com.example.data.model.HouseholdExpense
import com.example.data.model.Location
import com.example.data.model.MealPlanEntry
import com.example.data.model.MealSlot
import com.example.data.model.MovementType
import com.example.data.model.PantryItem
import com.example.data.model.ShoppingReminder
import com.example.data.model.StockMovement

@Entity(tableName = "pantry_items")
data class PantryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val qty: Double,
    val unit: String,
    val catId: String,
    val locationId: String = "despensa",
    val profileId: String = "p_main",
    val minQty: Double,
    val expDate: String,
    val barcode: String,
    val sub: String,
    val brand: String = "",
    val isArchived: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): PantryItem = PantryItem(
        id = id,
        name = name,
        qty = qty,
        unit = unit,
        cat = Category.fromId(catId),
        location = Location.fromId(locationId),
        profileId = profileId,
        minQty = minQty,
        expDate = expDate,
        barcode = barcode,
        sub = sub,
        brand = brand,
        isArchived = isArchived
    )

    companion object {
        fun fromDomain(item: PantryItem): PantryEntity = PantryEntity(
            id = item.id,
            name = item.name,
            qty = item.qty,
            unit = item.unit,
            catId = item.cat.id,
            locationId = item.location.id,
            profileId = item.profileId,
            minQty = item.minQty,
            expDate = item.expDate,
            barcode = item.barcode,
            sub = item.sub,
            brand = item.brand,
            isArchived = item.isArchived,
            updatedAt = System.currentTimeMillis()
        )
    }
}

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val itemName: String,
    val profileId: String,
    val type: String, // IN, OUT, ADJUST, WASTE, TRANSFER
    val delta: Double,
    val unit: String,
    val reason: String,
    val source: String,
    val timestamp: Long,
    val cost: Double = 0.0,
    val idempotencyKey: String
) {
    fun toDomain(): StockMovement = StockMovement(
        id = id,
        itemId = itemId,
        itemName = itemName,
        profileId = profileId,
        type = try { MovementType.valueOf(type) } catch (_: Exception) { MovementType.IN },
        delta = delta,
        unit = unit,
        reason = reason,
        source = source,
        timestamp = timestamp,
        cost = cost,
        idempotencyKey = idempotencyKey
    )

    companion object {
        fun fromDomain(m: StockMovement): StockMovementEntity = StockMovementEntity(
            id = m.id,
            itemId = m.itemId,
            itemName = m.itemName,
            profileId = m.profileId,
            type = m.type.name,
            delta = m.delta,
            unit = m.unit,
            reason = m.reason,
            source = m.source,
            timestamp = m.timestamp,
            cost = m.cost,
            idempotencyKey = m.idempotencyKey
        )
    }
}

@Entity(tableName = "shopping_items")
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val qty: Double,
    val unit: String,
    val estPrice: Double,
    val isChecked: Boolean,
    val profileId: String = "p_main",
    val source: String = "MANUAL", // LOW_STOCK, MANUAL, RECIPE, MEAL_PLAN
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "household_expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val storeName: String,
    val amount: Double,
    val itemCount: Int,
    val profileId: String = "p_main",
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "MANUAL",
    val notes: String = ""
) {
    fun toDomain(): HouseholdExpense = HouseholdExpense(
        id = id,
        storeName = storeName,
        amount = amount,
        itemCount = itemCount,
        profileId = profileId,
        timestamp = timestamp,
        source = source,
        notes = notes
    )

    companion object {
        fun fromDomain(e: HouseholdExpense): ExpenseEntity = ExpenseEntity(
            id = e.id,
            storeName = e.storeName,
            amount = e.amount,
            itemCount = e.itemCount,
            profileId = e.profileId,
            timestamp = e.timestamp,
            source = e.source,
            notes = e.notes
        )
    }
}

@Entity(tableName = "meal_plan_entries")
data class MealPlanEntryEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: Int, // 1..7
    val slot: String, // ALMUERZO, CENA
    val recipeId: Int,
    val recipeName: String,
    val servings: Int,
    val isCooked: Boolean
) {
    fun toDomain(): MealPlanEntry = MealPlanEntry(
        id = id,
        dayOfWeek = dayOfWeek,
        slot = try { MealSlot.valueOf(slot) } catch (_: Exception) { MealSlot.ALMUERZO },
        recipeId = recipeId,
        recipeName = recipeName,
        servings = servings,
        isCooked = isCooked
    )

    companion object {
        fun fromDomain(entry: MealPlanEntry): MealPlanEntryEntity = MealPlanEntryEntity(
            id = entry.id,
            dayOfWeek = entry.dayOfWeek,
            slot = entry.slot.name,
            recipeId = entry.recipeId,
            recipeName = entry.recipeName,
            servings = entry.servings,
            isCooked = entry.isCooked
        )
    }
}

@Entity(tableName = "shopping_reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dueDate: String,
    val isDone: Boolean,
    val createdAt: Long
) {
    fun toDomain(): ShoppingReminder = ShoppingReminder(
        id = id,
        name = name,
        dueDate = dueDate,
        isDone = isDone,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(reminder: ShoppingReminder): ReminderEntity = ReminderEntity(
            id = reminder.id,
            name = reminder.name,
            dueDate = reminder.dueDate,
            isDone = reminder.isDone,
            createdAt = reminder.createdAt
        )
    }
}
