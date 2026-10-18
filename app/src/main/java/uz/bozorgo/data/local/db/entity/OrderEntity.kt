package uz.bozorgo.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [Index(value = ["createdAt"])],
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val status: String,
    val createdAt: Long,
    val itemsJson: String,
    val itemsTotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val total: Double,
    val deliveryMethod: String,
    val paymentMethod: String,
    val recipientName: String,
    val phone: String,
    val addressTitle: String,
    val addressText: String,
    val comment: String,
    val isSynced: Boolean,
    val isPaid: Boolean,
)
