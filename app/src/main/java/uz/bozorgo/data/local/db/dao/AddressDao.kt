package uz.bozorgo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.bozorgo.data.local.db.entity.AddressEntity

@Dao
interface AddressDao {

    @Query("SELECT * FROM addresses ORDER BY isDefault DESC, createdAt DESC")
    fun observeAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getById(id: String): AddressEntity?

    @Query("SELECT * FROM addresses WHERE isDefault = 1 LIMIT 1")
    fun observeDefault(): Flow<AddressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefault()

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun markDefault(id: String)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun delete(id: String)
}
