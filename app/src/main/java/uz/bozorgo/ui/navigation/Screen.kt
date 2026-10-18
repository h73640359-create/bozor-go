package uz.bozorgo.ui.navigation

/** Ilova ekranlari va ularning marshrutlari. */
sealed class Screen(val route: String) {

    data object Home : Screen("home")
    data object Catalog : Screen("catalog")
    data object Cart : Screen("cart")
    data object Orders : Screen("orders")
    data object Profile : Screen("profile")

    data object Search : Screen("search")

    data object Category : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }

    data object Collection : Screen("collection/{type}") {
        fun createRoute(type: String) = "collection/$type"
        const val ARG_TYPE = "type"
    }

    data object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }

    data object Checkout : Screen("checkout")

    data object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(orderId: String) = "orders/$orderId"
    }

    data object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }

    data object EditProfile : Screen("edit_profile")

    data object Auth : Screen("auth")
    data object Addresses : Screen("addresses")
    data object Favorites : Screen("favorites")

    data object Info : Screen("info/{type}") {
        fun createRoute(type: String) = "info/$type"
        const val TYPE_HELP = "help"
        const val TYPE_PRIVACY = "privacy"
        const val TYPE_TERMS = "terms"
    }

    data object Settings : Screen("settings")

    companion object {
        /** Pastki navigatsiyada ko'rinadigan asosiy bo'limlar. */
        val bottomDestinations = listOf(Home.route, Catalog.route, Cart.route, Orders.route, Profile.route)
    }
}
