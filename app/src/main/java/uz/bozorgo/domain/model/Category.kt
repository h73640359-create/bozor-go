package uz.bozorgo.domain.model

data class Subcategory(
    val id: String,
    val name: String,
    val icon: String,
    val productCount: Int = 0,
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val productCount: Int = 0,
    val subcategories: List<Subcategory> = emptyList(),
)

data class Banner(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageAssetPath: String,
    val imageUrl: String = "",
    val actionType: String,
    val actionValue: String,
    val startColorHex: String,
    val endColorHex: String,
)
