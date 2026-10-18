package uz.bozorgo.core.util

import java.util.Locale

/**
 * Narxlarni O'zbekiston so'mida chiroyli chiqarish.
 * Masalan: 18900.0 -> "18 900 so'm"
 */
object MoneyFormatter {

    private const val CURRENCY = "so'm"

    fun formatUzs(amount: Double): String = "${groupDigits(amount.toLong())} $CURRENCY"

    fun formatUzsShort(amount: Double): String {
        val value = amount.toLong()
        return when {
            value >= 1_000_000 -> "${trim(value / 1_000_000.0)} mln $CURRENCY"
            value >= 1_000 -> "${trim(value / 1_000.0)} ming $CURRENCY"
            else -> "$value $CURRENCY"
        }
    }

    fun groupDigits(value: Long): String {
        val sign = if (value < 0) "-" else ""
        val digits = kotlin.math.abs(value).toString()
        val builder = StringBuilder()
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            builder.insert(0, digits[i])
            count++
            if (count % 3 == 0 && i > 0) builder.insert(0, '\u00A0')
        }
        return sign + builder.toString()
    }

    private fun trim(value: Double): String {
        val rounded = (value * 10).toLong() / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
    }
}
