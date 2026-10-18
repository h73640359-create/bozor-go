package uz.bozorgo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.local.db.dao.CartDao
import uz.bozorgo.data.local.db.entity.CartEntity
import uz.bozorgo.data.mapper.Mappers.toDomain
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.usecase.CartCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val calculator: CartCalculator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CartRepository {

    override fun observeItems(): Flow<List<CartItem>> = cartDao.observeCart()
        .map { rows -> rows.map { it.toDomain() } }

    override fun observeItemsCount(): Flow<Int> = cartDao.observeItemsCount()

    override fun observeSummary(deliveryMethod: DeliveryMethod): Flow<CartSummary> =
        observeItems().map { items -> calculator.calculate(items, deliveryMethod) }

    override suspend fun add(product: Product, quantity: Int, selectedOption: String?) =
        withContext(ioDispatcher) {
            if (!product.inStock || product.stock <= 0) return@withContext
            val id = key(product.id, selectedOption)
            val existing = cartDao.getById(id)
            val maxQuantity = product.stock.coerceAtLeast(1)
            if (existing == null) {
                cartDao.insert(
                    CartEntity(
                        id = id,
                        productId = product.id,
                        quantity = quantity.coerceIn(1, maxQuantity),
                        selectedOption = selectedOption.orEmpty(),
                    ),
                )
            } else {
                cartDao.updateQuantity(id, (existing.quantity + quantity).coerceIn(1, maxQuantity))
            }
        }

    override suspend fun setQuantity(productId: String, selectedOption: String?, quantity: Int) =
        withContext(ioDispatcher) {
            if (quantity <= 0) {
                remove(productId, selectedOption)
                return@withContext
            }
            val id = key(productId, selectedOption)
            val existing = cartDao.getById(id) ?: return@withContext
            cartDao.updateQuantity(id, quantity.coerceAtLeast(1))
        }

    override suspend fun remove(productId: String, selectedOption: String?) = withContext(ioDispatcher) {
        cartDao.deleteById(key(productId, selectedOption))
    }

    override suspend fun clear() = withContext(ioDispatcher) {
        cartDao.clear()
    }

    override suspend fun getItems(): List<CartItem> = withContext(ioDispatcher) {
        cartDao.getCartOnce().map { it.toDomain() }
    }

    companion object {
        /** Bitta mahsulotning turli variantlari alohida saqlanadi. */
        fun key(productId: String, selectedOption: String?): String =
            "$productId|${selectedOption.orEmpty()}"
    }
}
