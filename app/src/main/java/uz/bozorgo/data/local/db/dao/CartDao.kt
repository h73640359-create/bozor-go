package uz.bozorgo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import uz.bozorgo.data.local.db.entity.CartEntity
import uz.bozorgo.data.local.db.entity.CartWithProduct

@Dao
interface CartDao {

    @Transaction
    @Query("SELECT * FROM cart ORDER BY addedAt DESC")
    fun observeCart(): Flow<List<CartWithProduct>>

    @Transaction
    @Query("SELECT * FROM cart ORDER BY addedAt DESC")
    suspend fun getCartOnce(): List<CartWithProduct>

    @Query("SELECT * FROM cart WHERE id = :id")
    suspend fun getById(id: String): CartEntity?

    @Query("SELECT * FROM cart WHERE productId = :productId")
    suspend fun getByProductId(productId: String): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity)

    @Query("UPDATE cart SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("DELETE FROM cart WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cart")
    suspend fun clear()

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart")
    fun observeItemsCount(): Flow<Int>
}
