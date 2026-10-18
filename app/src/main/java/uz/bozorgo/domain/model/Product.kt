package uz.bozorgo.domain.model

/**
 * Katalogdagi mahsulot.
 *
 * Barcha maydonlar `data/catalog.json` (seed) yoki server javobidan keladi.
 * `imageAssetPath` — APK ichidagi lokal rasm, `imageUrl` — serverdagi rasm
 * (admin panel orqali yuklanganda to'ldiriladi).
 */
data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val subcategoryId: String,
    val subcategoryName: String,
    val brand: String,
    val imageAssetPath: String,
    val imageUrl: String = "",
    val description: String,
    val price: Double,
    val discountPrice: Double? = null,
    val discountPercent: Int = 0,
    val unit: String,
    val unitValue: Double = 1.0,
    val unitLabel: String,
    val stock: Int,
    val inStock: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val isPopular: Boolean = false,
    val isNew: Boolean = false,
    val isDeal: Boolean = false,
    val options: List<ProductOption> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: String = "",
    val isDemo: Boolean = true,
) {
    /** Amaldagi narx (chegirma hisobga olingan). */
    val effectivePrice: Double
        get() = if (discountPrice != null && discountPrice > 0 && discountPrice < price) discountPrice else price

    /** Chegirma bormi? */
    val hasDiscount: Boolean get() = effectivePrice < price

    /**
     * Rasm manbai: server URL bo'lsa shu, aks holda APK ichidagi asset.
     * Ikkalasi ham bo'sh bo'lsa `null` — bunda kategoriya rasmi chiziladi.
     */
    fun imageModelOrNull(): String? =
        when {
            imageUrl.isNotBlank() -> imageUrl
            imageAssetPath.isNotBlank() -> uz.bozorgo.core.common.Constants.ASSET_IMAGE_PREFIX + imageAssetPath
            else -> null
        }

    /** Rasm manbai — bo'sh bo'lsa kategoriya rasmi qaytadi (bo'sh kartalar qolmaydi). */
    fun imageModel(): String = imageModelOrNull() ?: fallbackImageModel()

    /**
     * Asosiy rasm yuklanmasa ko'rsatiladigan zaxira rasm.
     * Kategoriyaga mos (har bir kategoriyaning o'z rasmi), umumiy kulrang kvadrat emas.
     */
    fun fallbackImageModel(): String =
        uz.bozorgo.core.common.Constants.FALLBACK_IMAGE_PREFIX + categoryId + ".webp"
}

data class ProductOption(
    val name: String,
    val values: List<String>,
) {
    fun selectedOrDefault(selected: String?): String =
        values.firstOrNull { it == selected } ?: values.firstOrNull().orEmpty()
}
