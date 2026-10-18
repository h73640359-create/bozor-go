package uz.bozorgo.domain.model

enum class SortOption {
    POPULAR,
    PRICE_ASC,
    PRICE_DESC,
    NAME,
    NEWEST,
    DISCOUNT,
    ;

    companion object {
        fun fromKey(key: String): SortOption =
            entries.firstOrNull { it.name == key } ?: POPULAR
    }
}

/** Katalog / qidiruv filtri. */
data class ProductFilter(
    val categoryId: String? = null,
    val subcategoryId: String? = null,
    val query: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val onlyDiscount: Boolean = false,
    val inStockOnly: Boolean = false,
    val brands: List<String> = emptyList(),
    val sort: SortOption = SortOption.POPULAR,
) {
    fun isEmpty(): Boolean =
        categoryId == null && subcategoryId == null && query.isBlank() &&
            minPrice == null && maxPrice == null && !onlyDiscount && !inStockOnly &&
            brands.isEmpty() && sort == SortOption.POPULAR
}
