package uz.bozorgo.data.remote.dto

import com.google.gson.annotations.SerializedName
import uz.bozorgo.data.seed.dto.BannersSeedDto
import uz.bozorgo.data.seed.dto.CategoriesSeedDto
import uz.bozorgo.data.seed.dto.ProductSeedDto

// ------------------------------ Mahsulotlar --------------------------------

data class ProductsResponseDto(
    @SerializedName("data") val data: List<ProductSeedDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
)

data class CategoriesResponseDto(
    @SerializedName("categories") val categories: List<uz.bozorgo.data.seed.dto.CategorySeedDto>,
)

data class BannersResponseDto(
    @SerializedName("banners") val banners: List<uz.bozorgo.data.seed.dto.BannerSeedDto>,
)

// --------------------------- Autentifikatsiya -------------------------------

data class OtpRequestDto(
    @SerializedName("phone") val phone: String,
)

data class OtpRequestResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("demo") val demo: Boolean = true,
    @SerializedName("message") val message: String? = null,
    /** Faqat demo rejimda qaytariladi (haqiqiy SMS provayderi ulanmaganda). */
    @SerializedName("debugCode") val debugCode: String? = null,
)

data class OtpVerifyDto(
    @SerializedName("phone") val phone: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String? = null,
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto,
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String,
    @SerializedName("isAdmin") val isAdmin: Boolean = false,
)

// ------------------------------ Buyurtmalar --------------------------------

data class CreateOrderRequestDto(
    @SerializedName("idempotencyKey") val idempotencyKey: String,
    @SerializedName("items") val items: List<OrderItemRequestDto>,
    @SerializedName("deliveryMethod") val deliveryMethod: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("recipientName") val recipientName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("addressTitle") val addressTitle: String,
    @SerializedName("addressText") val addressText: String,
    @SerializedName("comment") val comment: String? = null,
)

data class OrderItemRequestDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("selectedOption") val selectedOption: String? = null,
)

data class OrderResponseDto(
    @SerializedName("order") val order: OrderDto,
)

data class OrdersResponseDto(
    @SerializedName("orders") val orders: List<OrderDto>,
)

data class OrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("items") val items: List<OrderItemDto>,
    @SerializedName("itemsTotal") val itemsTotal: Double,
    @SerializedName("discount") val discount: Double,
    @SerializedName("deliveryFee") val deliveryFee: Double,
    @SerializedName("total") val total: Double,
    @SerializedName("deliveryMethod") val deliveryMethod: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("recipientName") val recipientName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("addressTitle") val addressTitle: String?,
    @SerializedName("addressText") val addressText: String,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("isPaid") val isPaid: Boolean = false,
)

data class OrderItemDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("discountPrice") val discountPrice: Double? = null,
    @SerializedName("unit") val unit: String = "dona",
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("selectedOption") val selectedOption: String? = null,
)

// -------------------------------- To'lov -----------------------------------

data class PaymentRequestDto(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("method") val method: String,
)

data class PaymentResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("paymentUrl") val paymentUrl: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("demo") val demo: Boolean = true,
)
