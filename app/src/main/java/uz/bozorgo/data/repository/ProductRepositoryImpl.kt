package uz.bozorgo.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uz.bozorgo.core.common.Constants
import uz.bozorgo.core.common.Resource
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.local.db.ProductQueryBuilder
import uz.bozorgo.data.local.db.dao.ProductDao
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.data.mapper.Mappers.toDomain
import uz.bozorgo.data.mapper.Mappers.toEntity
import uz.bozorgo.data.remote.ApiConfig
import uz.bozorgo.data.remote.ApiService
import uz.bozorgo.data.seed.SeedImporter
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.model.ProductFilter
import uz.bozorgo.domain.model.SortOption
import uz.bozorgo.domain.repository.HomeSections
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val seedImporter: SeedImporter,
    private val preferences: UserPreferences,
    private val apiService: ApiService,
    private val apiConfig: ApiConfig,
    private val gson: Gson,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProductRepository {

    private val seedMutex = Mutex()

    override suspend fun ensureSeeded() = withContext(ioDispatcher) {
        seedMutex.withLock {
            val seededVersion = preferences.seedVersion.first()
            val currentCount = productDao.countAll()
            if (seededVersion >= Constants.SEED_VERSION && currentCount > 0) return@withLock
            val seed = seedImporter.readCatalog()
            productDao.insertAll(seed.products.map { it.toEntity(gson) })
            preferences.setSeedVersion(Constants.SEED_VERSION)
        }
    }

    override fun observeTotalCount(): Flow<Int> = productDao.observeCountAll()

    override suspend fun countTotal(): Int = withContext(ioDispatcher) { productDao.countAll() }

    override fun observeProducts(
        filter: ProductFilter,
        limit: Int,
        offset: Int,
    ): Flow<List<Product>> = productDao
        .observeProducts(ProductQueryBuilder.buildSelect(filter, limit, offset))
        .map { entities -> entities.map { it.toDomain(gson) } }

    override suspend fun getProducts(
        filter: ProductFilter,
        limit: Int,
        offset: Int,
    ): List<Product> = withContext(ioDispatcher) {
        productDao.getProducts(ProductQueryBuilder.buildSelect(filter, limit, offset))
            .map { it.toDomain(gson) }
    }

    override suspend fun countProducts(filter: ProductFilter): Int = withContext(ioDispatcher) {
        productDao.countProducts(ProductQueryBuilder.buildCount(filter))
    }

    override fun observeProduct(id: String): Flow<Product?> =
        productDao.observeById(id).map { it?.toDomain(gson) }

    override suspend fun getProduct(id: String): Product? = withContext(ioDispatcher) {
        productDao.getById(id)?.toDomain(gson)
    }

    override suspend fun getSimilar(productId: String, limit: Int): List<Product> =
        withContext(ioDispatcher) {
            val product = productDao.getById(productId) ?: return@withContext emptyList()
            productDao.getSimilar(product.subcategoryId, productId, limit).let { similar ->
                if (similar.size >= 4) {
                    similar.map { it.toDomain(gson) }
                } else {
                    // Kichik kategoriyada kam mahsulot bo'lsa — butun kategoriyadan to'ldiramiz
                    val fallback = productDao.getProducts(
                        ProductQueryBuilder.buildSelect(
                            ProductFilter(categoryId = product.categoryId, sort = SortOption.POPULAR),
                            limit + 1,
                            0,
                        ),
                    ).filter { it.id != productId }
                    (similar + fallback).distinctBy { it.id }.take(limit).map { it.toDomain(gson) }
                }
            }
        }

    override suspend fun getHomeSections(): HomeSections = withContext(ioDispatcher) {
        val popular = query(ProductFilter(sort = SortOption.POPULAR), 12)
        val newArrivals = query(ProductFilter(sort = SortOption.NEWEST), 12)
        val deals = query(ProductFilter(onlyDiscount = true, sort = SortOption.DISCOUNT), 12)
        val dailyIds = listOf(
            "fruits-vegetables",
            "dairy-eggs",
            "grocery-staples",
            "bakery-sweets",
        )
        val daily = dailyIds.flatMap { categoryId ->
            query(ProductFilter(categoryId = categoryId, inStockOnly = true), 6)
        }
        val recommended = query(ProductFilter(sort = SortOption.POPULAR), 24, offset = 12)
        HomeSections(
            popular = popular,
            newArrivals = newArrivals,
            deals = deals,
            daily = daily,
            recommended = recommended.ifEmpty { popular },
        )
    }

    override suspend fun priceRange(): Pair<Double, Double> = withContext(ioDispatcher) {
        (productDao.minPrice() ?: 0.0) to (productDao.maxPrice() ?: 1_000_000.0)
    }

    override suspend fun allBrands(): List<String> = withContext(ioDispatcher) {
        productDao.allBrands()
    }

    override suspend fun syncWithServer(): Resource<Int> = withContext(ioDispatcher) {
        if (!apiConfig.isConfigured) {
            return@withContext Resource.Error(
                message = "Server manzili sozlanmagan (API_BASE_URL)",
                isNetworkError = false,
            )
        }
        try {
            var synced = 0
            var offset = 0
            while (true) {
                val response = apiService.getProducts(limit = 200, offset = offset)
                if (response.data.isEmpty()) break
                productDao.upsertAll(response.data.map { it.toEntity(gson) })
                synced += response.data.size
                offset += response.data.size
                if (synced >= response.total) break
            }
            preferences.setLastSyncAt(System.currentTimeMillis())
            Resource.Success(synced)
        } catch (t: Throwable) {
            Resource.Error(
                message = t.message ?: "Server bilan sinxronlashda xatolik",
                cause = t,
                isNetworkError = true,
            )
        }
    }

    private suspend fun query(
        filter: ProductFilter,
        limit: Int,
        offset: Int = 0,
    ): List<Product> = productDao
        .getProducts(ProductQueryBuilder.buildSelect(filter, limit, offset))
        .map { it.toDomain(gson) }
}
