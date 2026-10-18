package uz.bozorgo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import uz.bozorgo.data.local.db.entity.ProductEntity

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    /**
     * Filtr, qidiruv va saralash bilan mahsulotlarni olish.
     * So'rov `ProductQueryBuilder` yordamida tuziladi (sahifalab yuklash uchun).
     */
    @RawQuery(observedEntities = [ProductEntity::class])
    fun observeProducts(query: SupportSQLiteQuery): Flow<List<ProductEntity>>

    @RawQuery(observedEntities = [ProductEntity::class])
    suspend fun getProducts(query: SupportSQLiteQuery): List<ProductEntity>

    @RawQuery
    suspend fun countProducts(query: SupportSQLiteQuery): Int

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM products")
    fun observeCountAll(): Flow<Int>

    /** O'xshash mahsulotlar: bir xil kichik kategoriyadan. */
    @Query(
        """
        SELECT * FROM products
        WHERE subcategoryId = :subcategoryId AND id != :productId
        ORDER BY rating DESC
        LIMIT :limit
        """,
    )
    suspend fun getSimilar(subcategoryId: String, productId: String, limit: Int): List<ProductEntity>

    /** Narx oralig'i (filtr ekrani uchun). */
    @Query("SELECT MIN(price) FROM products")
    suspend fun minPrice(): Double?

    @Query("SELECT MAX(price) FROM products")
    suspend fun maxPrice(): Double?

    @Query("SELECT DISTINCT brand FROM products ORDER BY brand ASC")
    suspend fun allBrands(): List<String>

    companion object {
        fun countQuery(where: String, args: Array<Any?>): SupportSQLiteQuery =
            SimpleSQLiteQuery("SELECT COUNT(*) FROM products $where", args)
    }
}
