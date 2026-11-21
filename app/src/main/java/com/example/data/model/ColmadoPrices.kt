package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class ColmadoItemPrice(
    val key: String,
    val displayName: String,
    val unit: String,
    val defaultPrice: Double,
    val isEstimated: Boolean = false
)

object ColmadoDefaults {
    val ITEMS = listOf(
        ColmadoItemPrice("arroz", "Arroz", "lb", 40.0, isEstimated = false),
        ColmadoItemPrice("habichuela", "Habichuelas rojas", "lb", 80.0, isEstimated = false),
        ColmadoItemPrice("guandul", "Guandules secos", "lb", 80.0, isEstimated = false),
        ColmadoItemPrice("ajo", "Ajo", "cabeza", 18.0, isEstimated = true),
        ColmadoItemPrice("cebolla", "Cebolla", "lb", 55.0, isEstimated = false),
        ColmadoItemPrice("aceite", "Aceite de cocina", "litro", 190.0, isEstimated = true),
        ColmadoItemPrice("tomate", "Tomate", "lb", 60.0, isEstimated = true),
        ColmadoItemPrice("pollo", "Pollo", "lb", 78.0, isEstimated = false),
        ColmadoItemPrice("platano", "Plátano", "unidad", 26.0, isEstimated = false),
        ColmadoItemPrice("huevo", "Huevos (por unidad)", "unidad", 8.0, isEstimated = false),
        ColmadoItemPrice("queso", "Queso", "lb", 280.0, isEstimated = true),
        ColmadoItemPrice("yuca", "Yuca", "lb", 37.0, isEstimated = false),
        ColmadoItemPrice("auyama", "Auyama", "lb", 29.0, isEstimated = false),
        ColmadoItemPrice("molida", "Carne molida", "lb", 190.0, isEstimated = true),
        ColmadoItemPrice("papa", "Papa", "lb", 45.0, isEstimated = false),
        ColmadoItemPrice("pescado", "Pescado fresco", "lb", 150.0, isEstimated = true),
        ColmadoItemPrice("limon", "Limón", "unidad", 10.0, isEstimated = true),
        ColmadoItemPrice("cilantro", "Cilantro", "ramito", 20.0, isEstimated = true),
        ColmadoItemPrice("pasta", "Espagueti o pasta", "paquete", 45.0, isEstimated = true),
        ColmadoItemPrice("fideo", "Fideos", "funda", 15.0, isEstimated = true),
        ColmadoItemPrice("avena", "Avena", "lb", 40.0, isEstimated = true),
        ColmadoItemPrice("leche", "Leche", "litro", 105.0, isEstimated = true),
        ColmadoItemPrice("canela", "Canela", "palito", 10.0, isEstimated = true),
        ColmadoItemPrice("azucar", "Azúcar", "lb", 40.0, isEstimated = true),
        ColmadoItemPrice("atun", "Atún", "lata", 85.0, isEstimated = true),
        ColmadoItemPrice("pan", "Pan", "unidad", 12.0, isEstimated = true),
        ColmadoItemPrice("guineo", "Guineo", "unidad", 8.0, isEstimated = true),
        ColmadoItemPrice("harina", "Harina de maíz", "lb", 45.0, isEstimated = true),
        ColmadoItemPrice("batata", "Batata", "lb", 35.0, isEstimated = false),
        ColmadoItemPrice("cond", "Sal, orégano y sazón", "paquetico", 25.0, isEstimated = true)
    )

    fun formatRD(amount: Double): String {
        val rounded = Math.round(amount)
        val format = NumberFormat.getIntegerInstance(Locale("es", "DO"))
        return "RD$${format.format(rounded)}"
    }

    fun formatQuantity(qty: Double, unit: String): String {
        if (unit == "litro" && qty < 1.0) {
            return "${Math.round(qty * 1000)} ml"
        }
        val intVal = qty.toInt()
        val frac = qty - intVal
        val fracStr = when {
            Math.abs(frac - 0.25) < 0.05 -> "¼"
            Math.abs(frac - 0.5) < 0.05 -> "½"
            Math.abs(frac - 0.75) < 0.05 -> "¾"
            frac > 0.05 -> String.format(Locale.US, "%.1f", qty)
            else -> ""
        }
        val text = if (fracStr.isNotEmpty() && (fracStr == "¼" || fracStr == "½" || fracStr == "¾")) {
            if (intVal > 0) "$intVal $fracStr" else fracStr
        } else if (fracStr.isNotEmpty()) {
            fracStr
        } else {
            "$intVal"
        }

        val pluralUnit = when (unit) {
            "unidad" -> if (qty <= 1.0) "unidad" else "unidades"
            "lb" -> "lb"
            "litro" -> if (qty <= 1.0) "litro" else "litros"
            else -> if (qty <= 1.0) unit else "${unit}s"
        }
        return "$text $pluralUnit"
    }
}
