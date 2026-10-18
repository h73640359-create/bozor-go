package uz.bozorgo.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["subcategoryId"]),
        Index(value = ["price"]),
        Index(value = ["brand"]),
    ],
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val subcategoryId: String,
    val subcategoryName: String,
    val brand: String,
    val imageAssetPath: String,
    val imageUrl: String,
    val description: String,
    val price: Double,
    val discountPrice: Double?,
    val discountPercent: Int,
    val unit: String,
    val unitValue: Double,
    val unitLabel: String,
    val stock: Int,
    val inStock: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val isPopular: Boolean,
    val isNew: Boolean,
    val isDeal: Boolean,
    val optionsJson: String,
    val tagsJson: String,
    val createdAt: String,
    val isDemo: Boolean,
)
