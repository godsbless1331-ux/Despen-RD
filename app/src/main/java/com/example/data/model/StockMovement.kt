package com.example.data.model

import java.util.UUID

enum class MovementType(val displayName: String, val emoji: String) {
    IN("Entrada (Compra)", "📥"),
    OUT("Consumo", "📤"),
    ADJUST("Ajuste manual", "⚖️"),
    WASTE("Merma / Vencido", "🗑️"),
    TRANSFER("Transferencia", "🔁")
}

data class StockMovement(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val itemName: String,
    val profileId: String = "p_main",
    val type: MovementType = MovementType.IN,
    val delta: Double,
    val unit: String = "ud",
    val reason: String = "",
    val source: String = "MANUAL", // MANUAL, SCAN, RECEIPT, RECIPE, RESTOCK
    val timestamp: Long = System.currentTimeMillis(),
    val cost: Double = 0.0,
    val idempotencyKey: String = UUID.randomUUID().toString()
)
