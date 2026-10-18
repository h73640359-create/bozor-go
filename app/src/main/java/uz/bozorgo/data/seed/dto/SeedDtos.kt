package uz.bozorgo.data.seed.dto

import com.google.gson.annotations.SerializedName

/** data/catalog.json fayli strukturasi. */
data class CatalogSeedDto(
    @SerializedName("version") val version: Int,
    @SerializedName("generatedAt") val generatedAt: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("isDemoData") val isDemoData: Boolean,
    @SerializedName("notice") val notice: String,
    @SerializedName("productCount") val productCount: Int,
    @SerializedName("products") val products: List<ProductSeedDto>,
)

data class ProductSeedDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("subcategoryId") val subcategoryId: String,
    @SerializedName("subcategoryName") val subcategoryName: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("image") val image: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("discountPrice") val discountPrice: Double?,
    @SerializedName("discountPercent") val discountPercent: Int,
    @SerializedName("unit") val unit: String,
    @SerializedName("unitValue") val unitValue: Double,
    @SerializedName("unitLabel") val unitLabel: String,
    @SerializedName("stock") val stock: Int,
    @SerializedName("inStock") val inStock: Boolean,
    @SerializedName("rating") val rating: Double,
    @SerializedName("reviewCount") val reviewCount: Int,
    @SerializedName("isPopular") val isPopular: Boolean,
    @SerializedName("isNew") val isNew: Boolean,
    @SerializedName("isDeal") val isDeal: Boolean,
    @SerializedName("options") val options: List<OptionSeedDto>,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("isDemo") val isDemo: Boolean,
)

data class OptionSeedDto(
    @SerializedName("name") val name: String,
    @SerializedName("values") val values: List<String>,
)

data class CategoriesSeedDto(
    @SerializedName("categories") val categories: List<CategorySeedDto>,
)

data class CategorySeedDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("color") val color: String,
    @SerializedName("productCount") val productCount: Int,
    @SerializedName("subcategories") val subcategories: List<SubcategorySeedDto>,
)

data class SubcategorySeedDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("productCount") val productCount: Int,
)

data class BannersSeedDto(
    @SerializedName("banners") val banners: List<BannerSeedDto>,
)

data class BannerSeedDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("image") val image: String,
    @SerializedName("actionType") val actionType: String,
    @SerializedName("actionValue") val actionValue: String,
    @SerializedName("startColor") val startColor: String,
    @SerializedName("endColor") val endColor: String,
)
