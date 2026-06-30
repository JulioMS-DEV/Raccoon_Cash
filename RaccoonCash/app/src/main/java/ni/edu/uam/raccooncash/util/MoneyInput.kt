package ni.edu.uam.raccooncash.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private const val maxMoneyIntegerDigits = 12
private val moneySymbols = DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = ','
    decimalSeparator = '.'
}

fun isPotentialMoneyInput(value: String): Boolean {
    if (value.isEmpty()) return true
    return value.count { it.isDigit() } <= maxMoneyIntegerDigits + 2
}

fun parseMoneyInput(value: String): Double? {
    val normalizedInput = value.trim()
        .replace('٫', '.')
        .replace('，', ',')
        .replace('．', '.')

    val moneyChars = normalizedInput.mapNotNull { char ->
        when {
            char == ',' || char == '.' -> char
            char.isDigit() -> char.digitToIntOrNull()?.digitToChar()
            else -> null
        }
    }.joinToString("")
    if (moneyChars.isBlank() || moneyChars.none { it.isDigit() }) {
        return null
    }

    val lastSeparatorIndex = maxOf(moneyChars.lastIndexOf('.'), moneyChars.lastIndexOf(','))
    val decimalDigits = if (lastSeparatorIndex >= 0) {
        moneyChars.substring(lastSeparatorIndex + 1).filter { it.isDigit() }
    } else {
        ""
    }
    val hasDecimalSeparator = lastSeparatorIndex >= 0 && decimalDigits.length in 1..2

    val normalized = if (hasDecimalSeparator) {
        val integerDigits = moneyChars.substring(0, lastSeparatorIndex).filter { it.isDigit() }.ifBlank { "0" }
        "$integerDigits.$decimalDigits"
    } else {
        moneyChars.filter { it.isDigit() }
    }

    val integerPart = normalized.substringBefore('.')
    if (integerPart.count { it.isDigit() } > maxMoneyIntegerDigits) {
        return null
    }

    return normalized.toDoubleOrNull()
}

fun formatMoneyAmount(value: Double, precision: Int = 2): String {
    val safePrecision = precision.coerceAtLeast(0)
    return moneyFormatter(
        minimumFractionDigits = safePrecision,
        maximumFractionDigits = safePrecision
    ).format(value)
}

fun formatCurrencyAmount(value: Double, currency: String = "C$", precision: Int = 2): String {
    val sign = if (value < 0.0) "-" else ""
    return "$sign$currency${formatMoneyAmount(abs(value), precision)}"
}

fun formatEditableMoney(value: Double?): String {
    if (value == null) return ""
    return moneyFormatter(
        minimumFractionDigits = 0,
        maximumFractionDigits = 2
    ).format(value)
}

private fun moneyFormatter(minimumFractionDigits: Int, maximumFractionDigits: Int): DecimalFormat {
    return DecimalFormat().apply {
        decimalFormatSymbols = moneySymbols
        isGroupingUsed = true
        this.minimumFractionDigits = minimumFractionDigits
        this.maximumFractionDigits = maximumFractionDigits
    }
}
