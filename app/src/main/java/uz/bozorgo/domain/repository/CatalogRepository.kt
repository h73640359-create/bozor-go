package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.domain.model.Banner
import uz.bozorgo.domain.model.Category

interface CatalogRepository {

    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategories(): List<Category>

    suspend fun getCategory(id: String): Category?

    fun observeBanners(): Flow<List<Banner>>

    suspend fun getBanners(): List<Banner>
}
