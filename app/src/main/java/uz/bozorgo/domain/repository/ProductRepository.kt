package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.core.common.Resource
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.model.ProductFilter

/** Bosh sahifa uchun tayyor bo'limlar. */
data class HomeSections(
    val popular: List<Product>,
    val newArrivals: List<Product>,
    val deals: List<Product>,
    val daily: List<Product>,
    val recommended: List<Product>,
)

interface ProductRepository {

    /** Birinchi ishga tushirishda katalog lokal bazaga yoziladi. */
    suspend fun ensureSeeded()

    fun observeTotalCount(): Flow<Int>

    suspend fun countTotal(): Int

    fun observeProducts(filter: ProductFilter, limit: Int, offset: Int): Flow<List<Product>>

    suspend fun getProducts(filter: ProductFilter, limit: Int, offset: Int): List<Product>

    suspend fun countProducts(filter: ProductFilter): Int

    fun observeProduct(id: String): Flow<Product?>

    suspend fun getProduct(id: String): Product?

    suspend fun getSimilar(productId: String, limit: Int = 12): List<Product>

    suspend fun getHomeSections(): HomeSections

    suspend fun priceRange(): Pair<Double, Double>

    suspend fun allBrands(): List<String>

    /** Server bilan sinxronlash (server sozlanmagan bo'lsa — o'tkazib yuboriladi). */
    suspend fun syncWithServer(): Resource<Int>
}
