package uz.bozorgo.domain.model

import uz.bozorgo.core.common.Constants

enum class OrderStatus(val key: String) {
    CREATED("created"),
    CONFIRMED("confirmed"),
    PREPARING("preparing"),
    COURIER("courier"),
    DELIVERING("delivering"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    val canBeCancelled: Boolean
        get() = this == CREATED || this == CONFIRMED || this == PREPARING

    /** Holatlar ketma-ketligi (kuzatish ekrani uchun). */
    val stepIndex: Int get() = ordinal

    companion object {
        fun fromKey(key: String): OrderStatus =
            entries.firstOrNull { it.key == key } ?: CREATED
    }
}

enum class PaymentMethod(val key: String) {
    CASH("cash"),
    CARD("card"),
}

enum class DeliveryMethod(val key: String) {
    STANDARD("standard"),
    EXPRESS("express"),
    PICKUP("pickup"),
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val discountPrice: Double? = null,
    val unit: String = "dona",
    val imageAssetPath: String = "",
    val imageUrl: String = "",
    val selectedOption: String? = null,
) {
    val lineTotal: Double get() = (discountPrice ?: unitPrice) * quantity

    /**
     * Buyurtma tarkibidagi mahsulot rasmi.
     * Buyurtma yaratilganda rasm manbasi saqlab qolingan, shuning uchun
     * tarix va tafsilot ekranlari ham rasmni ko'rsata oladi.
     */
    fun imageModelOrNull(): String? = when {
        imageUrl.isNotBlank() -> imageUrl
        imageAssetPath.isNotBlank() -> Constants.ASSET_IMAGE_PREFIX + imageAssetPath
        else -> null
    }

    /** Zaxira manba: identifikator bo'yicha lokal rasm. */
    fun fallbackImageModel(): String = Constants.ASSET_IMAGE_PREFIX + "products/" + productId + ".webp"
}

data class Order(
    val id: String,
    val orderNumber: String,
    val status: OrderStatus,
    val createdAt: Long,
    val items: List<OrderItem>,
    val itemsTotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val total: Double,
    val deliveryMethod: DeliveryMethod,
    val paymentMethod: PaymentMethod,
    val recipientName: String,
    val phone: String,
    val addressTitle: String,
    val addressText: String,
    val comment: String = "",
    val isSynced: Boolean = false,
    val isPaid: Boolean = false,
) {
    val itemsCount: Int get() = items.sumOf { it.quantity }
}
