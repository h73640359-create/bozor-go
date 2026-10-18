package uz.bozorgo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val title: String,
    val city: String,
    val district: String,
    val street: String,
    val house: String,
    val apartment: String = "",
    val comment: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
