package uz.bozorgo.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.bozorgo.core.common.Resource
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.local.db.dao.OrderDao
import uz.bozorgo.data.mapper.Mappers.toDomain
import uz.bozorgo.data.mapper.Mappers.toEntity
import uz.bozorgo.data.remote.ApiConfig
import uz.bozorgo.data.remote.ApiService
import uz.bozorgo.data.remote.dto.CreateOrderRequestDto
import uz.bozorgo.data.remote.dto.OrderDto
import uz.bozorgo.data.remote.dto.OrderItemRequestDto
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.model.OrderItem
import uz.bozorgo.domain.model.OrderStatus
import uz.bozorgo.domain.model.PaymentMethod
import uz.bozorgo.domain.repository.CreateOrderParams
import uz.bozorgo.domain.repository.OrderRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Buyurtmalar.
 *
 * MUHIM: server sozlangan bo'lsa, buyurtma faqat server muvaffaqiyatli
 * yozgandan keyingina "qabul qilindi" deb qaytariladi. Server sozlanmagan
 * bo'lsa — buyurtma faqat qurilmada saqlanadi (demo/oflayn rejim) va bu
 * foydalanuvchiga alohida xabar bilan ko'rsatiladi.
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val apiService: ApiService,
    private val apiConfig: ApiConfig,
    private val gson: Gson,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : OrderRepository {

    override fun observeOrders(): Flow<List<Order>> =
        orderDao.observeOrders().map { rows -> rows.map { it.toDomain(gson) } }

    override fun observeOrder(id: String): Flow<Order?> =
        orderDao.observeOrder(id).map { it?.toDomain(gson) }

    override suspend fun createOrder(params: CreateOrderParams): Resource<Order> =
        withContext(ioDispatcher) {
            val now = System.currentTimeMillis()
            val localId = UUID.randomUUID().toString()
            val items = params.items.map { cartItem ->
                OrderItem(
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.product.price,
                    discountPrice = cartItem.product.discountPrice,
                    unit = cartItem.product.unit,
                    imageAssetPath = cartItem.product.imageAssetPath,
                    imageUrl = cartItem.product.imageUrl,
                    selectedOption = cartItem.selectedOption,
                )
            }

            if (apiConfig.isConfigured) {
                return@withContext try {
                    val response = apiService.createOrder(
                        CreateOrderRequestDto(
                            idempotencyKey = localId,
                            items = params.items.map {
                                OrderItemRequestDto(
                                    productId = it.product.id,
                                    quantity = it.quantity,
                                    selectedOption = it.selectedOption,
                                )
                            },
                            deliveryMethod = params.deliveryMethod.key,
                            paymentMethod = params.paymentMethod.key,
                            recipientName = params.recipientName,
                            phone = params.phone,
                            addressTitle = params.addressTitle,
                            addressText = params.addressText,
                            comment = params.comment.ifBlank { null },
                        ),
                    )
                    val order = response.order.toDomain().copy(isSynced = true)
                    orderDao.insert(order.toEntity(gson))
                    Resource.Success(order)
                } catch (t: Throwable) {
                    Resource.Error(
                        message = t.message ?: "Buyurtmani serverga yozib bo'lmadi",
                        cause = t,
                        isNetworkError = true,
                    )
                }
            }

            // Server sozlanmagan: buyurtma faqat qurilmada saqlanadi
            val order = Order(
                id = localId,
                orderNumber = generateOrderNumber(now),
                status = OrderStatus.CREATED,
                createdAt = now,
                items = items,
                itemsTotal = params.summary.itemsTotal,
                discount = params.summary.discount,
                deliveryFee = params.summary.deliveryFee,
                total = params.summary.total,
                deliveryMethod = params.deliveryMethod,
                paymentMethod = params.paymentMethod,
                recipientName = params.recipientName,
                phone = params.phone,
                addressTitle = params.addressTitle,
                addressText = params.addressText,
                comment = params.comment,
                isSynced = false,
                isPaid = false,
            )
            orderDao.insert(order.toEntity(gson))
            Resource.Success(order)
        }

    override suspend fun cancelOrder(id: String): Resource<Order> = withContext(ioDispatcher) {
        val current = orderDao.getById(id)?.toDomain(gson)
            ?: return@withContext Resource.Error("Buyurtma topilmadi")
        if (!current.status.canBeCancelled) {
            return@withContext Resource.Error(
                "Bu holatdagi buyurtmani bekor qilib bo'lmaydi (${current.status.key})",
            )
        }
        if (apiConfig.isConfigured) {
            try {
                val response = apiService.cancelOrder(id)
                val updated = response.order.toDomain().copy(isSynced = true)
                orderDao.insert(updated.toEntity(gson))
                return@withContext Resource.Success(updated)
            } catch (t: Throwable) {
                return@withContext Resource.Error(
                    message = t.message ?: "Bekor qilib bo'lmadi",
                    cause = t,
                    isNetworkError = true,
                )
            }
        }
        val updated = current.copy(status = OrderStatus.CANCELLED)
        orderDao.insert(updated.toEntity(gson))
        Resource.Success(updated)
    }

    override suspend fun syncPendingOrders(): Resource<Int> = withContext(ioDispatcher) {
        if (!apiConfig.isConfigured) {
            return@withContext Resource.Error("Server manzili sozlanmagan (API_BASE_URL)")
        }
        val pending = orderDao.getUnsynced()
        if (pending.isEmpty()) return@withContext Resource.Success(0)
        var synced = 0
        for (entity in pending) {
            val order = entity.toDomain(gson)
            runCatching {
                apiService.createOrder(
                    CreateOrderRequestDto(
                        idempotencyKey = order.id,
                        items = order.items.map {
                            OrderItemRequestDto(
                                productId = it.productId,
                                quantity = it.quantity,
                                selectedOption = it.selectedOption,
                            )
                        },
                        deliveryMethod = order.deliveryMethod.key,
                        paymentMethod = order.paymentMethod.key,
                        recipientName = order.recipientName,
                        phone = order.phone,
                        addressTitle = order.addressTitle,
                        addressText = order.addressText,
                        comment = order.comment.ifBlank { null },
                    ),
                )
            }.onSuccess { response ->
                val updated = response.order.toDomain().copy(isSynced = true)
                orderDao.insert(updated.toEntity(gson))
                synced++
            }.onFailure {
                // Xatolik bo'lsa keyingi urinishda qayta sinab ko'ramiz
            }
        }
        Resource.Success(synced)
    }

    private fun generateOrderNumber(timeMillis: Long): String {
        val suffix = (timeMillis % 1_000_000).toString().padStart(6, '0')
        return "BG-$suffix"
    }

    private fun OrderDto.toDomain(): Order = Order(
        id = id,
        orderNumber = orderNumber,
        status = OrderStatus.fromKey(status),
        createdAt = parseIsoDateTime(createdAt),
        items = items.map { item ->
            OrderItem(
                productId = item.productId,
                productName = item.productName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                discountPrice = item.discountPrice,
                unit = item.unit,
                imageUrl = item.imageUrl.orEmpty(),
                selectedOption = item.selectedOption,
            )
        },
        itemsTotal = itemsTotal,
        discount = discount,
        deliveryFee = deliveryFee,
        total = total,
        deliveryMethod = runCatching { DeliveryMethod.valueOf(deliveryMethod.uppercase()) }
            .getOrDefault(DeliveryMethod.STANDARD),
        paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod.uppercase()) }
            .getOrDefault(PaymentMethod.CASH),
        recipientName = recipientName,
        phone = phone,
        addressTitle = addressTitle.orEmpty(),
        addressText = addressText,
        comment = comment.orEmpty(),
        isPaid = isPaid,
        isSynced = true,
    )

    companion object {
        /**
         * ISO-8601 sana vaqtini millisekundga o'tkazish.
         * (java.time o'rniga SimpleDateFormat — minSdk 24 qo'llab-quvvatlanadi.)
         */
        private val ISO_PARSER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun parseIsoDateTime(value: String): Long {
            val cleaned = value
                .replace("Z", "")
                .substringBefore('+')
                .substringBefore('.')
            return runCatching { ISO_PARSER.parse(cleaned)?.time }
                .getOrNull() ?: System.currentTimeMillis()
        }
    }
}
