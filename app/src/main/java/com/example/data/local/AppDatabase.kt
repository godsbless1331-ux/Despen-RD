package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.MovementType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        PantryEntity::class,
        StockMovementEntity::class,
        ShoppingListItemEntity::class,
        ExpenseEntity::class,
        MealPlanEntryEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
    abstract fun movementDao(): StockMovementDao
    abstract fun shoppingDao(): ShoppingListDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "despensa_pro_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val pantryDao = database.pantryDao()
            val movementDao = database.movementDao()
            val shoppingDao = database.shoppingDao()
            val mealPlanDao = database.mealPlanDao()

            val initialPantry = listOf(
                PantryEntity("p1", "Arroz Selecto", 5.0, "lb", "alimento", "despensa", "p_main", 2.0, "2027-04-15", "746123456001", ""),
                PantryEntity("p2", "Habichuelas Rojas", 2.0, "lb", "alimento", "despensa", "p_main", 1.0, "2027-02-10", "746123456002", ""),
                PantryEntity("p3", "Plátano Verde", 6.0, "ud", "alimento", "despensa", "p_main", 3.0, "2026-11-12", "", ""),
                PantryEntity("p4", "Huevos Criollos", 12.0, "ud", "alimento", "nevera", "p_main", 4.0, "2026-11-20", "", ""),
                PantryEntity("p5", "Pollo Fresco Criollo", 3.0, "lb", "alimento", "congelador", "p_main", 1.0, "2026-11-18", "", ""),
                PantryEntity("p6", "Queso Geo de Freír", 1.0, "lb", "alimento", "nevera", "p_main", 0.5, "2026-11-25", "746123456004", ""),
                PantryEntity("p7", "Aceite Vegetal Crisol", 1.0, "l", "alimento", "despensa", "p_main", 0.5, "2027-08-30", "746123456003", ""),
                PantryEntity("p8", "Cloro Macier", 1.0, "l", "limpieza", "despensa", "p_main", 0.5, "2028-01-01", "", ""),
                PantryEntity("p9", "Pañales Huggies Talla 3", 40.0, "ud", "bebe", "despensa", "p_baby", 10.0, "2028-12-31", "", "panales")
            )
            pantryDao.insertAll(initialPantry)

            // Seed initial ledger movements
            initialPantry.forEach { item ->
                movementDao.insertMovement(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = item.id,
                        itemName = item.name,
                        profileId = item.profileId,
                        type = MovementType.IN.name,
                        delta = item.qty,
                        unit = item.unit,
                        reason = "Inventario inicial",
                        source = "INITIAL",
                        timestamp = System.currentTimeMillis() - 86400000L,
                        cost = 0.0,
                        idempotencyKey = "init_${item.id}"
                    )
                )
            }

            // Seed initial shopping list items
            shoppingDao.insertAll(
                listOf(
                    ShoppingListItemEntity(UUID.randomUUID().toString(), "Cebolla roja", 2.0, "lb", 80.0, false, "p_main", "LOW_STOCK"),
                    ShoppingListItemEntity(UUID.randomUUID().toString(), "Ajo en pasta o cabeza", 1.0, "ud", 45.0, false, "p_main", "MANUAL"),
                    ShoppingListItemEntity(UUID.randomUUID().toString(), "Toallitas húmedas bebé", 1.0, "paq", 120.0, false, "p_baby", "MANUAL")
                )
            )

            // Seed initial meal plan
            mealPlanDao.insertOrUpdate(
                MealPlanEntryEntity(UUID.randomUUID().toString(), 1, "ALMUERZO", 1, "La Bandera Dominicana", 4, false)
            )
            mealPlanDao.insertOrUpdate(
                MealPlanEntryEntity(UUID.randomUUID().toString(), 1, "CENA", 2, "Mangú con los tres golpes", 4, false)
            )
            mealPlanDao.insertOrUpdate(
                MealPlanEntryEntity(UUID.randomUUID().toString(), 2, "ALMUERZO", 3, "Moro de habichuelas con pollo guisado", 4, false)
            )
        }
    }
}
