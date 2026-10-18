package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.core.common.Resource
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.model.PaymentMethod

data class CreateOrderParams(
    val items: List<uz.bozorgo.domain.model.CartItem>,
    val summary: uz.bozorgo.domain.model.CartSummary,
    val deliveryMethod: DeliveryMethod,
    val paymentMethod: PaymentMethod,
    val recipientName: String,
    val phone: String,
    val addressTitle: String,
    val addressText: String,
    val comment: String = "",
)

interface OrderRepository {

    fun observeOrders(): Flow<List<Order>>

    fun observeOrder(id: String): Flow<Order?>

    suspend fun createOrder(params: CreateOrderParams): Resource<Order>

    suspend fun cancelOrder(id: String): Resource<Order>

    /** Internet paydo bo'lganda yuborilmagan buyurtmalarni serverga yozish. */
    suspend fun syncPendingOrders(): Resource<Int>
}
