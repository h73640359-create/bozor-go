package uz.bozorgo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.bozorgo.data.local.db.entity.OrderEntity

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrder(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE orders SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncState(id: String, synced: Boolean)

    @Query("UPDATE orders SET isPaid = :paid WHERE id = :id")
    suspend fun updatePaidState(id: String, paid: Boolean)

    @Query("SELECT * FROM orders WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsynced(): List<OrderEntity>

    @Query("SELECT COUNT(*) FROM orders")
    fun observeCount(): Flow<Int>
}
