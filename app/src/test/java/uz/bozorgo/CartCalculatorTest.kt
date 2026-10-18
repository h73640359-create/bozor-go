package uz.bozorgo

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.usecase.CartCalculator

class CartCalculatorTest {

    private val calculator = CartCalculator()

    private fun product(
        id: String,
        price: Double,
        discount: Double? = null,
        stock: Int = 100,
    ) = Product(
        id = id,
        name = "Mahsulot $id",
        categoryId = "grocery-staples",
        categoryName = "Oziq-ovqat",
        subcategoryId = "rice",
        subcategoryName = "Guruch",
        brand = "Demo brand",
        imageAssetPath = "products/$id.webp",
        description = "Demo tavsif",
        price = price,
        discountPrice = discount,
        unit = "kg",
        unitLabel = "1 kg",
        stock = stock,
        inStock = stock > 0,
        rating = 4.5,
        reviewCount = 10,
    )

    @Test
    fun emptyCartHasNoFees() {
        val summary = calculator.calculate(emptyList(), DeliveryMethod.STANDARD)
        assertThat(summary.itemsCount).isEqualTo(0)
        assertThat(summary.itemsTotal).isEqualTo(0.0)
        assertThat(summary.deliveryFee).isEqualTo(0.0)
        assertThat(summary.total).isEqualTo(0.0)
    }

    @Test
    fun discountsAreCounted() {
        val items = listOf(
            CartItem(product("p1", 20_000.0, 15_000.0), 2),
            CartItem(product("p2", 10_000.0), 1),
        )
        val summary = calculator.calculate(items, DeliveryMethod.PICKUP)
        assertThat(summary.itemsCount).isEqualTo(3)
        assertThat(summary.itemsTotal).isEqualTo(40_000.0)
        assertThat(summary.discount).isEqualTo(10_000.0)
        assertThat(summary.deliveryFee).isEqualTo(0.0) // pickup bepul
        assertThat(summary.total).isEqualTo(40_000.0)
    }

    @Test
    fun deliveryIsFreeAboveThreshold() {
        val items = listOf(CartItem(product("p1", 100_000.0), 1))
        val summary = calculator.calculate(items, DeliveryMethod.STANDARD)
        assertThat(summary.deliveryFee).isEqualTo(0.0)
        assertThat(calculator.amountToFreeDelivery(100_000.0)).isEqualTo(0.0)
    }

    @Test
    fun deliveryFeeBelowThreshold() {
        val items = listOf(CartItem(product("p1", 40_000.0), 1))
        assertThat(calculator.calculate(items, DeliveryMethod.STANDARD).deliveryFee).isEqualTo(15_000.0)
        assertThat(calculator.calculate(items, DeliveryMethod.EXPRESS).deliveryFee).isEqualTo(25_000.0)
        assertThat(calculator.amountToFreeDelivery(40_000.0)).isEqualTo(60_000.0)
    }

    @Test
    fun totalIncludesDelivery() {
        val items = listOf(CartItem(product("p1", 60_000.0), 1))
        val summary = calculator.calculate(items, DeliveryMethod.EXPRESS)
        assertThat(summary.total).isEqualTo(85_000.0)
    }
}
