package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllItems(): Flow<List<PantryEntity>>

    @Query("SELECT * FROM pantry_items WHERE isArchived = 0 AND (profileId = :profileId OR :profileId = 'all') ORDER BY name ASC")
    fun getItemsByProfile(profileId: String): Flow<List<PantryEntity>>

    @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): PantryEntity?

    @Query("SELECT * FROM pantry_items WHERE barcode = :barcode AND isArchived = 0 LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): PantryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PantryEntity>)

    @Update
    suspend fun updateItem(item: PantryEntity)

    @Query("UPDATE pantry_items SET isArchived = 1 WHERE id = :id")
    suspend fun archiveById(id: String)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pantry_items")
    suspend fun clearAll()
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE profileId = :profileId OR :profileId = 'all' ORDER BY timestamp DESC")
    fun getMovementsByProfile(profileId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getMovementsSince(sinceTimestamp: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Query("DELETE FROM stock_movements")
    suspend fun clearAll()
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_items ORDER BY isChecked ASC, createdAt DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE profileId = :profileId OR :profileId = 'all' ORDER BY isChecked ASC, createdAt DESC")
    fun getShoppingItemsByProfile(profileId: String): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Query("UPDATE shopping_items SET isChecked = :isChecked WHERE id = :id")
    suspend fun setChecked(id: String, isChecked: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    suspend fun deleteChecked()

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM household_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM household_expenses WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getExpensesSince(sinceTimestamp: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM household_expenses")
    suspend fun clearAll()
}

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plan_entries ORDER BY dayOfWeek ASC, slot ASC")
    fun getAllEntries(): Flow<List<MealPlanEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: MealPlanEntryEntity)

    @Query("UPDATE meal_plan_entries SET isCooked = :isCooked WHERE id = :id")
    suspend fun setCooked(id: String, isCooked: Boolean)

    @Query("DELETE FROM meal_plan_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM meal_plan_entries")
    suspend fun clearAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM shopping_reminders ORDER BY isDone ASC, createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM shopping_reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shopping_reminders WHERE isDone = 1")
    suspend fun clearCompleted()

    @Query("DELETE FROM shopping_reminders")
    suspend fun clearAll()
}
