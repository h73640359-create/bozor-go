package uz.bozorgo.data.local.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: String,
    val addedAt: Long = System.currentTimeMillis(),
)

data class FavoriteWithProduct(
    @Embedded val favorite: FavoriteEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity,
)
