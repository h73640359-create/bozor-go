package uz.bozorgo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.mapper.Mappers.toDomain
import uz.bozorgo.data.remote.ApiConfig
import uz.bozorgo.data.remote.ApiService
import uz.bozorgo.data.seed.SeedImporter
import uz.bozorgo.domain.model.Banner
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.repository.CatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kategoriya va bannerlar.
 *
 * Asosiy manba — APK ichidagi seed fayllari (oflayn ishlash kafolati).
 * Server sozlangan bo'lsa, yangilangan ro'yxat serverdan olinadi.
 */
@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val seedImporter: SeedImporter,
    private val apiService: ApiService,
    private val apiConfig: ApiConfig,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CatalogRepository {

    private val mutex = Mutex()
    private var loaded = false

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())

    override fun observeCategories(): Flow<List<Category>> = flow {
        ensureLoaded()
        emitAll(_categories.asStateFlow())
    }

    override fun observeBanners(): Flow<List<Banner>> = flow {
        ensureLoaded()
        emitAll(_banners.asStateFlow())
    }

    override suspend fun getCategories(): List<Category> {
        ensureLoaded()
        return _categories.value
    }

    override suspend fun getCategory(id: String): Category? = getCategories().firstOrNull { it.id == id }

    override suspend fun getBanners(): List<Banner> {
        ensureLoaded()
        return _banners.value
    }

    private suspend fun ensureLoaded() = withContext(ioDispatcher) {
        mutex.withLock {
            if (loaded) return@withLock
            _categories.value = seedImporter.readCategories().categories.map { it.toDomain() }
            _banners.value = seedImporter.readBanners().banners.map { it.toDomain() }
            loaded = true
        }
        if (apiConfig.isConfigured) {
            runCatching {
                _categories.value = apiService.getCategories().categories.map { it.toDomain() }
                _banners.value = apiService.getBanners().banners.map { it.toDomain() }
            }
        }
    }
}
