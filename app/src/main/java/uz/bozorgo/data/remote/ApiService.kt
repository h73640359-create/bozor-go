package uz.bozorgo.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import uz.bozorgo.data.remote.dto.AuthResponseDto
import uz.bozorgo.data.remote.dto.BannersResponseDto
import uz.bozorgo.data.remote.dto.CategoriesResponseDto
import uz.bozorgo.data.remote.dto.CreateOrderRequestDto
import uz.bozorgo.data.remote.dto.OtpRequestDto
import uz.bozorgo.data.remote.dto.OtpRequestResponseDto
import uz.bozorgo.data.remote.dto.OtpVerifyDto
import uz.bozorgo.data.remote.dto.OrderResponseDto
import uz.bozorgo.data.remote.dto.OrdersResponseDto
import uz.bozorgo.data.remote.dto.PaymentRequestDto
import uz.bozorgo.data.remote.dto.PaymentResponseDto
import uz.bozorgo.data.remote.dto.ProductsResponseDto

/**
 * BozorGo REST API interfeysi (Retrofit).
 *
 * Backend (`backend/` papkasi) shu kontrakt bo'yicha ishlaydi.
 * To'liq hujjat: `docs/API.md`.
 */
interface ApiService {

    // ---- Mahsulotlar ----
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("updatedAfter") updatedAfter: String? = null,
    ): ProductsResponseDto

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): uz.bozorgo.data.seed.dto.ProductSeedDto

    // ---- Katalog metadatasi ----
    @GET("categories")
    suspend fun getCategories(): CategoriesResponseDto

    @GET("banners")
    suspend fun getBanners(): BannersResponseDto

    // ---- Autentifikatsiya (telefon + SMS kod) ----
    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestDto): OtpRequestResponseDto

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyDto): AuthResponseDto

    // ---- Buyurtmalar ----
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequestDto): OrderResponseDto

    @GET("orders")
    suspend fun getOrders(): OrdersResponseDto

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderResponseDto

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): OrderResponseDto

    // ---- To'lov ----
    @POST("payments/create")
    suspend fun createPayment(@Body body: PaymentRequestDto): PaymentResponseDto
}
