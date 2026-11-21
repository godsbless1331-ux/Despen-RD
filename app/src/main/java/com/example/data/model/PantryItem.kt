package com.example.data.model

import java.util.UUID

enum class Category(val id: String, val emoji: String, val displayName: String) {
    ALIMENTO("alimento", "🍎", "Alimentos"),
    BEBIDA("bebida", "🥤", "Bebidas"),
    LIMPIEZA("limpieza", "🧽", "Limpieza"),
    HIGIENE("higiene", "🧴", "Higiene"),
    HOGAR("hogar", "🏠", "Hogar"),
    MASCOTA("mascota", "🐾", "Mascotas"),
    BEBE("bebe", "👶", "Bebé"),
    OTRO("otro", "📦", "Otros");

    companion object {
        fun fromId(id: String): Category = entries.find { it.id == id } ?: OTRO
    }
}

enum class Location(val id: String, val emoji: String, val displayName: String) {
    PANTRY("despensa", "🥫", "Despensa"),
    FRIDGE("nevera", "❄️", "Nevera"),
    FREEZER("congelador", "🧊", "Congelador");

    companion object {
        fun fromId(id: String): Location = entries.find { it.id == id } ?: PANTRY
    }
}

enum class BabySubCategory(val id: String, val displayName: String) {
    PANALES("panales", "Pañales"),
    TOALLITAS("toallitas", "Toallitas y cambio"),
    ALIMENTACION("alimentacion", "Alimentación"),
    BANO("bano", "Baño e higiene"),
    UTENSILIOS("utensilios", "Utensilios"),
    ROPA("ropa", "Ropa y otros");

    companion object {
        fun fromId(id: String): BabySubCategory? = entries.find { it.id == id }

        fun guessSub(name: String): BabySubCategory {
            val n = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replace(Regex("\\p{M}"), "")
                .lowercase()
                .trim()
            return when {
                n.contains("panalera") || n.contains("mochila") -> UTENSILIOS
                n.contains("toallit") || n.contains("cambiador") || n.contains("crema") -> TOALLITAS
                n.contains("panal") -> PANALES
                n.contains("biberon") || n.contains("formula") || n.contains("papilla") || n.contains("cereal") || n.contains("compota") -> ALIMENTACION
                n.contains("champu") || n.contains("shampoo") || n.contains("jabon") || n.contains("talco") || n.contains("colonia") || n.contains("banera") -> BANO
                n.contains("ropa") || n.contains("body") || n.contains("media") || n.contains("gorro") || n.contains("pijama") -> ROPA
                else -> UTENSILIOS
            }
        }
    }
}

data class PantryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val qty: Double,
    val unit: String = "ud",
    val cat: Category = Category.ALIMENTO,
    val location: Location = Location.PANTRY,
    val profileId: String = "p_main",
    val minQty: Double = 0.0,
    val expDate: String = "", // YYYY-MM-DD
    val barcode: String = "",
    val sub: String = "", // BabySubCategory ID if cat == BEBE
    val brand: String = "",
    val isArchived: Boolean = false
) {
    fun daysUntilExpiry(): Long? {
        if (expDate.isBlank()) return null
        return try {
            val parts = expDate.split("-").map { it.toInt() }
            if (parts.size != 3) return null
            val calTarget = java.util.Calendar.getInstance().apply {
                set(parts[0], parts[1] - 1, parts[2], 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val calToday = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val diffMs = calTarget.timeInMillis - calToday.timeInMillis
            diffMs / (1000 * 60 * 60 * 24)
        } catch (_: Exception) {
            null
        }
    }

    val isExpired: Boolean
        get() {
            val d = daysUntilExpiry() ?: return false
            return d < 0
        }

    fun isExpiringSoon(thresholdDays: Int): Boolean {
        val d = daysUntilExpiry() ?: return false
        return d in 0..thresholdDays
    }

    val isLowStock: Boolean
        get() = minQty > 0 && qty <= minQty
}

val SUPPORTED_UNITS = listOf("ud", "kg", "g", "lb", "l", "ml", "paq", "lata", "caja")
