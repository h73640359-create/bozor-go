package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.domain.model.Product

interface FavoriteRepository {

    fun observeFavorites(): Flow<List<Product>>

    fun observeIds(): Flow<Set<String>>

    fun observeIsFavorite(productId: String): Flow<Boolean>

    suspend fun toggle(productId: String): Boolean

    suspend fun remove(productId: String)
}
