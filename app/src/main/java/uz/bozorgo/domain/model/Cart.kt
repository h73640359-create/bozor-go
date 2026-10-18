package uz.bozorgo.domain.model

/** Savatchadagi mahsulot (tanlangan varianti bilan). */
data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedOption: String? = null,
) {
    val lineTotal: Double get() = product.effectivePrice * quantity
    val lineOldTotal: Double get() = product.price * quantity
    val lineDiscount: Double get() = (lineOldTotal - lineTotal).coerceAtLeast(0.0)
}

/** Savatcha hisob-kitobi. */
data class CartSummary(
    val itemsCount: Int,
    val itemsTotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val total: Double,
) {
    companion object {
        fun empty() = CartSummary(0, 0.0, 0.0, 0.0, 0.0)
    }
}
