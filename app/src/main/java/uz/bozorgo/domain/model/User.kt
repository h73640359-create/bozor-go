package uz.bozorgo.domain.model

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val isAdmin: Boolean = false,
)

data class Address(
    val id: String,
    val title: String,
    val city: String,
    val district: String,
    val street: String,
    val house: String,
    val apartment: String = "",
    val comment: String = "",
    val isDefault: Boolean = false,
) {
    /** To'liq manzil matni. */
    val fullAddress: String
        get() = listOf(city, district, street, house, apartment)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}
