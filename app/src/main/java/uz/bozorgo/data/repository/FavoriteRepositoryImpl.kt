package uz.bozorgo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.local.db.dao.FavoriteDao
import uz.bozorgo.data.local.db.entity.FavoriteEntity
import uz.bozorgo.data.mapper.Mappers.toDomain
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.repository.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FavoriteRepository {

    override fun observeFavorites(): Flow<List<Product>> =
        favoriteDao.observeFavorites().map { rows -> rows.map { it.toDomain() } }

    override fun observeIds(): Flow<Set<String>> =
        favoriteDao.observeIds().map { it.toSet() }

    override fun observeIsFavorite(productId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(productId)

    /** true — sevimlilarga qo'shildi, false — olib tashlandi. */
    override suspend fun toggle(productId: String): Boolean = withContext(ioDispatcher) {
        val isFavorite = favoriteDao.isFavorite(productId)
        if (isFavorite) {
            favoriteDao.delete(productId)
        } else {
            favoriteDao.insert(FavoriteEntity(productId = productId))
        }
        !isFavorite
    }

    override suspend fun remove(productId: String) = withContext(ioDispatcher) {
        favoriteDao.delete(productId)
    }
}
