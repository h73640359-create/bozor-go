package uz.bozorgo.data.local.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "cart")
data class CartEntity(
    /** productId + "|" + selectedOption — bitta mahsulotning turli variantlari alohida saqlanadi. */
    @PrimaryKey val id: String,
    val productId: String,
    val quantity: Int,
    val selectedOption: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)

data class CartWithProduct(
    @Embedded val cart: CartEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity,
)
