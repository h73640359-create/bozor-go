package uz.bozorgo.domain.usecase

import uz.bozorgo.core.common.Constants
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.DeliveryMethod
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Savatcha summasini hisoblash (sof Kotlin — unit testlar bilan tekshiriladi).
 *
 * Yetkazib berish narxi:
 *  * PICKUP  — bepul
 *  * EXPRESS — 25 000 so'm
 *  * STANDARD — 100 000 so'mdan past buyurtmada 15 000 so'm, undan yuqorida bepul
 */
@Singleton
class CartCalculator @Inject constructor() {

    fun calculate(
        items: List<CartItem>,
        deliveryMethod: DeliveryMethod = DeliveryMethod.STANDARD,
    ): CartSummary {
        val itemsCount = items.sumOf { it.quantity }
        val itemsTotal = items.sumOf { it.product.price * it.quantity }
        val payable = items.sumOf { it.lineTotal }
        val discount = (itemsTotal - payable).coerceAtLeast(0.0)
        val deliveryFee = deliveryFeeFor(payable, deliveryMethod)
        return CartSummary(
            itemsCount = itemsCount,
            itemsTotal = payable,
            discount = discount,
            deliveryFee = deliveryFee,
            total = payable + deliveryFee,
        )
    }

    fun deliveryFeeFor(itemsTotal: Double, deliveryMethod: DeliveryMethod): Double =
        when (deliveryMethod) {
            DeliveryMethod.PICKUP -> 0.0
            DeliveryMethod.EXPRESS -> Constants.EXPRESS_DELIVERY_FEE
            DeliveryMethod.STANDARD -> if (itemsTotal <= 0.0) {
                0.0
            } else if (itemsTotal >= Constants.FREE_DELIVERY_THRESHOLD) {
                0.0
            } else {
                Constants.DELIVERY_FEE
            }
        }

    /** Bepul yetkazib berish uchun yetishmayotgan summa. */
    fun amountToFreeDelivery(itemsTotal: Double): Double =
        (Constants.FREE_DELIVERY_THRESHOLD - itemsTotal).coerceAtLeast(0.0)
}
