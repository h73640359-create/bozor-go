package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Product

interface CartRepository {

    fun observeItems(): Flow<List<CartItem>>

    fun observeItemsCount(): Flow<Int>

    fun observeSummary(deliveryMethod: DeliveryMethod = DeliveryMethod.STANDARD): Flow<CartSummary>

    suspend fun add(product: Product, quantity: Int = 1, selectedOption: String? = null)

    suspend fun setQuantity(productId: String, selectedOption: String?, quantity: Int)

    suspend fun remove(productId: String, selectedOption: String?)

    suspend fun clear()

    suspend fun getItems(): List<CartItem>
}
