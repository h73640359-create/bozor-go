package uz.bozorgo.core.util

/**
 * O'zbek (lotin) matni bilan ishlash uchun yordamchi funksiyalar.
 *
 * Qidiruvda katta-kichik harf va turli xil apostroflar (O' / O‘ / O’) farq
 * qilmasligi uchun matn normallashtiriladi.
 */
object TextNormalizer {

    private val APOSTROPHES = charArrayOf('’', '‘', 'ʼ', 'ʻ', '`', '´')

    fun normalize(value: String): String {
        val builder = StringBuilder(value.length)
        for (ch in value) {
            val c = if (ch in APOSTROPHES) '\'' else ch
            builder.append(c.lowercaseChar())
        }
        return builder.toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /** So'rovni alohida so'zlarga ajratish (AND qidiruv uchun). */
    fun tokens(value: String): List<String> =
        normalize(value).split(' ').filter { it.length >= 2 }

    /** Matn so'rovning barcha so'zlarini o'z ichiga oladimi? */
    fun matches(haystack: String, query: String): Boolean {
        if (query.isBlank()) return true
        val normalized = normalize(haystack)
        return tokens(query).all { token -> normalized.contains(token) }
    }
}
