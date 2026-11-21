package com.example.data.model

data class ParsedReceiptLine(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var qty: Double,
    var unit: String,
    var category: Category,
    var sub: String = "",
    var unitPrice: Double = 0.0,
    var confidence: Double = 0.9, // 0.0 to 1.0
    var isIncluded: Boolean = true
) {
    val confidenceLevel: ConfidenceLevel
        get() = when {
            confidence >= 0.85 -> ConfidenceLevel.HIGH
            confidence >= 0.65 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
}

enum class ConfidenceLevel(val displayName: String) {
    HIGH("Alta confianza"),
    MEDIUM("Revisar"),
    LOW("Dudoso")
}

data class ParsedReceiptResult(
    val storeName: String = "Colmado / Supermercado",
    val date: String = "",
    val totalAmount: Double = 0.0,
    val lines: List<ParsedReceiptLine>
)
