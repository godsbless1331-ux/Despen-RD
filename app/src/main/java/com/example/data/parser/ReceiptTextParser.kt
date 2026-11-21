package com.example.data.parser

import com.example.data.model.BabySubCategory
import com.example.data.model.Category
import com.example.data.model.PantryItem
import com.example.data.model.ParsedReceiptLine
import com.example.data.model.ParsedReceiptResult
import java.util.Locale

object ReceiptTextParser {

    private val IGNORE_PATTERN = Regex(
        "total|subtotal|itbis|impuesto|cambio|efectivo|tarjeta|factura|ncf|rnc|fecha|gracias|cajero|balance|ticket",
        RegexOption.IGNORE_CASE
    )

    private val QUANTITY_UNIT_PATTERN = Regex(
        """(\d+(?:[.,]\d+)?)\s*(kg|g|lbs|lb|lt|l|ml|uds|und|ud|paq|caja|lata)\b""",
        RegexOption.IGNORE_CASE
    )

    private val LEADING_QTY_PATTERN = Regex(
        """^(\d+)\s*[xX]?\s+(?=\D)"""
    )

    private val PRICE_PATTERN = Regex(
        """(?:[-–—:]\s*)?(?:RD)?\$?\s*(\d+(?:[.,]\d{2})?)\s*$""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String): List<PantryItem> {
        return parseDetailed(text).lines.filter { it.isIncluded }.map { line ->
            PantryItem(
                name = line.name,
                qty = line.qty,
                unit = line.unit,
                cat = line.category,
                sub = line.sub
            )
        }
    }

    fun parseDetailed(text: String): ParsedReceiptResult {
        val lines = text.split("\n", "\r")
        val parsedLines = mutableListOf<ParsedReceiptLine>()
        var storeName = "Supermercado / Colmado"
        var totalAmount = 0.0

        for ((index, rawLine) in lines.withIndex()) {
            var line = rawLine.trim()
            if (line.length < 2) continue

            // Look for store header in first 3 lines
            if (index < 3 && (line.contains("Sirena", ignoreCase = true) ||
                        line.contains("Nacional", ignoreCase = true) ||
                        line.contains("Bravo", ignoreCase = true) ||
                        line.contains("Jumbo", ignoreCase = true) ||
                        line.contains("Olé", ignoreCase = true) ||
                        line.contains("Colmado", ignoreCase = true) ||
                        line.contains("Plaza Lama", ignoreCase = true))) {
                storeName = line
                continue
            }

            // Look for total
            if (line.contains("total", ignoreCase = true)) {
                val matchPrice = PRICE_PATTERN.find(line)
                if (matchPrice != null) {
                    val priceStr = matchPrice.groupValues[1].replace(',', '.')
                    totalAmount = priceStr.toDoubleOrNull() ?: totalAmount
                }
                continue
            }

            if (IGNORE_PATTERN.containsMatchIn(line)) continue

            var qty = 1.0
            var unit = "ud"
            var price = 0.0
            var confidence = 0.90

            // Try to extract price from the end
            val priceMatch = PRICE_PATTERN.find(line)
            if (priceMatch != null) {
                val pStr = priceMatch.groupValues[1].replace(',', '.')
                price = pStr.toDoubleOrNull() ?: 0.0
                line = line.removeRange(priceMatch.range).trim()
            }

            val matchUnit = QUANTITY_UNIT_PATTERN.find(line)
            if (matchUnit != null) {
                val numStr = matchUnit.groupValues[1].replace(',', '.')
                qty = numStr.toDoubleOrNull() ?: 1.0
                unit = normalizeUnit(matchUnit.groupValues[2])
                line = line.removeRange(matchUnit.range).trim()
            } else {
                val matchLeading = LEADING_QTY_PATTERN.find(line)
                if (matchLeading != null) {
                    qty = matchLeading.groupValues[1].toDoubleOrNull() ?: 1.0
                    line = line.removeRange(matchLeading.range).trim()
                } else {
                    confidence -= 0.15 // Lower confidence if quantity was inferred
                }
            }

            line = line.replace(Regex("""[^\p{L}\p{N}\s\-]"""), " ")
            line = line.replace(Regex("""\s+"""), " ").trim()
            line = line.trim { it <= ' ' || it == '-' || it == '–' || it == '—' || it == ':' }

            if (line.length < 2 || line.matches(Regex("""^\d+$"""))) continue

            val formattedName = line.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val category = guessCategory(formattedName)
            val sub = if (category == Category.BEBE) BabySubCategory.guessSub(formattedName).id else ""

            if (price == 0.0) {
                // If price wasn't on line, approximate from colmado reference
                price = when (category) {
                    Category.ALIMENTO -> 85.0 * qty
                    Category.BEBE -> 120.0 * qty
                    Category.LIMPIEZA -> 95.0 * qty
                    else -> 75.0 * qty
                }
                confidence -= 0.10
            }

            parsedLines.add(
                ParsedReceiptLine(
                    name = formattedName,
                    qty = if (qty > 0) qty else 1.0,
                    unit = unit,
                    category = category,
                    sub = sub,
                    unitPrice = price,
                    confidence = Math.max(0.50, Math.min(0.98, confidence)),
                    isIncluded = true
                )
            )
        }

        if (totalAmount <= 0.0) {
            totalAmount = parsedLines.sumOf { it.unitPrice }
        }

        return ParsedReceiptResult(
            storeName = storeName,
            totalAmount = totalAmount,
            lines = parsedLines
        )
    }

    private fun normalizeUnit(u: String): String {
        return when (u.lowercase()) {
            "lbs" -> "lb"
            "lt" -> "l"
            "und", "uds" -> "ud"
            "kg", "g", "lb", "l", "ml", "paq", "caja", "lata", "ud" -> u.lowercase()
            else -> "ud"
        }
    }

    private fun normalize(s: String): String {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase()
            .trim()
    }

    fun guessCategory(name: String): Category {
        val n = normalize(name)
        return when {
            n.contains("panal") || n.contains("biberon") || n.contains("chupete") ||
                    n.contains("formula infantil") || n.contains("papilla") ||
                    n.contains("toallit") || n.contains("mordedor") ||
                    n.contains("babero") || n.contains("bebe") -> Category.BEBE

            n.contains("perro") || n.contains("gato") || n.contains("mascota") ||
                    n.contains("dog") || n.contains("cat chow") -> Category.MASCOTA

            n.contains("champu") || n.contains("shampoo") || n.contains("jabon") ||
                    n.contains("pasta dental") || n.contains("cepillo") ||
                    n.contains("papel higienico") || n.contains("desodorante") ||
                    n.contains("toalla sanitaria") || n.contains("crema") -> Category.HIGIENE

            n.contains("cloro") || n.contains("detergente") || n.contains("suavizante") ||
                    n.contains("desinfectante") || n.contains("fregador") ||
                    n.contains("escoba") || n.contains("limpiador") ||
                    n.contains("lavaplatos") -> Category.LIMPIEZA

            n.contains("bombillo") || n.contains("pila") || n.contains("bateria") ||
                    n.contains("funda") || n.contains("bolsa") || n.contains("vela") ||
                    n.contains("fosforo") || n.contains("servilleta") -> Category.HOGAR

            n.contains("agua") || n.contains("refresco") || n.contains("jugo") ||
                    n.contains("cerveza") || n.contains("ron") || n.contains("vino") ||
                    n.contains("gaseosa") || n.contains("cafe") || n.contains("te ") ||
                    n.contains("malta") || n.contains("bebida") -> Category.BEBIDA

            else -> Category.ALIMENTO
        }
    }
}
