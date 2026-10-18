package uz.bozorgo.data.local.db

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import uz.bozorgo.core.util.TextNormalizer
import uz.bozorgo.domain.model.ProductFilter
import uz.bozorgo.domain.model.SortOption

/**
 * Mahsulotlar uchun SQL so'rov tuzuvchi.
 *
 * Tartiblash va filtrlar dinamik bo'lgani uchun `@RawQuery` ishlatiladi.
 * Barcha foydalanuvchi kiritgan qiymatlar `?` orqali bog'lanadi (SQL injection
 * himoyasi), tartiblash ustuni esa faqat ruxsat etilgan ro'yxatdan olinadi.
 */
object ProductQueryBuilder {

    private const val TABLE = "products"

    fun buildSelect(filter: ProductFilter, limit: Int, offset: Int): SupportSQLiteQuery {
        val where = buildWhere(filter)
        val sql = buildString {
            append("SELECT * FROM $TABLE ")
            append(where.clause)
            append(" ORDER BY ")
            append(orderBy(filter.sort))
            append(" LIMIT ? OFFSET ?")
        }
        val args = where.args.toMutableList().apply {
            add(limit)
            add(offset)
        }.toTypedArray()
        return SimpleSQLiteQuery(sql, args)
    }

    fun buildCount(filter: ProductFilter): SupportSQLiteQuery {
        val where = buildWhere(filter)
        return SimpleSQLiteQuery("SELECT COUNT(*) FROM $TABLE ${where.clause}", where.args)
    }

    private fun buildWhere(filter: ProductFilter): Where {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        val tokens = TextNormalizer.tokens(filter.query)
        for (token in tokens) {
            val like = "%${escapeLike(token)}%"
            conditions.add(
                """
                (LOWER(name) LIKE ? ESCAPE '\'
                 OR LOWER(brand) LIKE ? ESCAPE '\'
                 OR LOWER(categoryName) LIKE ? ESCAPE '\'
                 OR LOWER(subcategoryName) LIKE ? ESCAPE '\'
                 OR LOWER(tagsJson) LIKE ? ESCAPE '\')
                """.trimIndent(),
            )
            repeat(5) { args.add(like) }
        }

        if (!filter.categoryId.isNullOrBlank()) {
            conditions.add("categoryId = ?")
            args.add(filter.categoryId)
        }
        if (!filter.subcategoryId.isNullOrBlank()) {
            conditions.add("subcategoryId = ?")
            args.add(filter.subcategoryId)
        }
        if (filter.minPrice != null) {
            conditions.add("price >= ?")
            args.add(filter.minPrice)
        }
        if (filter.maxPrice != null) {
            conditions.add("price <= ?")
            args.add(filter.maxPrice)
        }
        if (filter.onlyDiscount) {
            conditions.add("discountPrice IS NOT NULL AND discountPrice > 0")
        }
        if (filter.inStockOnly) {
            conditions.add("inStock = 1")
        }
        if (filter.brands.isNotEmpty()) {
            val placeholders = filter.brands.joinToString(",") { "?" }
            conditions.add("brand IN ($placeholders)")
            args.addAll(filter.brands)
        }

        val clause = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        return Where(clause, args.toTypedArray())
    }

    private fun orderBy(sort: SortOption): String = when (sort) {
        SortOption.POPULAR -> "rating DESC, reviewCount DESC, name ASC"
        SortOption.PRICE_ASC -> "price ASC, name ASC"
        SortOption.PRICE_DESC -> "price DESC, name ASC"
        SortOption.NAME -> "name COLLATE NOCASE ASC"
        SortOption.NEWEST -> "createdAt DESC, id DESC"
        SortOption.DISCOUNT -> "discountPercent DESC, rating DESC"
    }

    /** LIKE uchun maxsus belgilarni ekranlash. */
    private fun escapeLike(value: String): String = value
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")

    private class Where(val clause: String, val args: Array<Any?>)
}
