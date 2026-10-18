package uz.bozorgo.core.util

/**
 * O'zbekiston telefon raqamini tekshirish.
 *
 * Qabul qilinadi: +998 90 123 45 67, 998901234567, 90 123 45 67 …
 * Qabul qilinmaydi: 3 tadan kam xonali, 12 tadan ortiq xonali satrlar.
 */
fun isValidUzPhone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length in 9..12
}
