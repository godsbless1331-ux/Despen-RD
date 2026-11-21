package com.example.data.model

import java.util.UUID

data class ShoppingReminder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dueDate: String = "", // YYYY-MM-DD
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun daysUntilDue(): Long? {
        if (dueDate.isBlank()) return null
        return try {
            val parts = dueDate.split("-").map { it.toInt() }
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
}

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val source: String,
    val itemCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
