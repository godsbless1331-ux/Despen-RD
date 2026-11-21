package com.example.data.model

import java.util.UUID

enum class MealSlot(val displayName: String, val emoji: String) {
    DESAYUNO("Desayuno", "🌅"),
    ALMUERZO("Almuerzo", "☀️"),
    CENA("Cena", "🌙")
}

data class MealPlanEntry(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: Int, // 1 = Lunes, 2 = Martes, ..., 7 = Domingo
    val slot: MealSlot = MealSlot.ALMUERZO,
    val recipeId: Int,
    val recipeName: String,
    val servings: Int = 4,
    val isCooked: Boolean = false
) {
    val dayName: String
        get() = when (dayOfWeek) {
            1 -> "Lunes"
            2 -> "Martes"
            3 -> "Miércoles"
            4 -> "Jueves"
            5 -> "Viernes"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> "Día $dayOfWeek"
        }
}
