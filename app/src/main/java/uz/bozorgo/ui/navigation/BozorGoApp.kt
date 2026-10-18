package uz.bozorgo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uz.bozorgo.ui.components.LoadingState
import uz.bozorgo.ui.screens.address.AddressesRoute
import uz.bozorgo.ui.screens.auth.AuthRoute
import uz.bozorgo.ui.screens.cart.CartRoute
import uz.bozorgo.ui.screens.catalog.CatalogRootRoute
import uz.bozorgo.ui.screens.catalog.CatalogRoute
import uz.bozorgo.ui.screens.checkout.CheckoutRoute
import uz.bozorgo.ui.screens.favorites.FavoritesRoute
import uz.bozorgo.ui.screens.home.HomeRoute
import uz.bozorgo.ui.screens.info.InfoRoute
import uz.bozorgo.ui.screens.orders.OrderDetailRoute
import uz.bozorgo.ui.screens.orders.OrderSuccessRoute
import uz.bozorgo.ui.screens.orders.OrdersRoute
import uz.bozorgo.ui.screens.product.ProductDetailRoute
import uz.bozorgo.ui.screens.profile.EditProfileRoute
import uz.bozorgo.ui.screens.profile.ProfileRoute
import uz.bozorgo.ui.screens.search.SearchRoute
import uz.bozorgo.ui.screens.settings.SettingsRoute
import androidx.compose.ui.res.stringResource
import uz.bozorgo.R

/**
 * Ilovaning yagona navigatsiya grafigi.
 *
 * Pastki navigatsiya faqat 5 ta asosiy bo'limda ko'rinadi; qolgan
 * ekranlar (qidiruv, mahsulot, rasmiylashtirish, manzillar …) ustki
 * qatlam sifatida ochiladi.
 */
@Composable
fun BozorGoApp(
    cartBadgeCount: Int,
    isOnline: Boolean,
    isPreparing: Boolean,
    catalogCount: Int,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Screen.bottomDestinations

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BozorGoBottomBar(
                    navController = navController,
                    cartBadgeCount = cartBadgeCount,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isPreparing && catalogCount == 0) {
                LoadingState(modifier = Modifier.fillMaxSize())
            } else {
                BozorGoNavHost(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    isOnline = isOnline,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding,
                )
            }
        }
    }
}

@Composable
private fun BozorGoNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    val openProduct: (String) -> Unit = { productId ->
        navController.navigateSingleTop(Screen.ProductDetail.createRoute(productId))
    }

    val openTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
    ) {
        composable(Screen.Home.route) {
            HomeRoute(
                contentPadding = contentPadding,
                onNavigateToSearch = { navController.navigateSingleTop(Screen.Search.route) },
                onNavigateToCategory = { categoryId ->
                    navController.navigateSingleTop(Screen.Category.createRoute(categoryId))
                },
                onNavigateToProduct = openProduct,
                onNavigateToCollection = { type ->
                    navController.navigateSingleTop(Screen.Collection.createRoute(type))
                },
                onNavigateToFavorites = { navController.navigateSingleTop(Screen.Favorites.route) },
                onNavigateToAddresses = { navController.navigateSingleTop(Screen.Addresses.route) },
                onNavigateToOrders = { openTab(Screen.Orders.route) },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Catalog.route) {
            CatalogRootRoute(
                contentPadding = contentPadding,
                onCategoryClick = { categoryId ->
                    navController.navigateSingleTop(Screen.Category.createRoute(categoryId))
                },
                onSearchClick = { navController.navigateSingleTop(Screen.Search.route) },
            )
        }

        composable(
            route = Screen.Category.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
        ) {
            CatalogRoute(
                title = "",
                contentPadding = contentPadding,
                onBackClick = { navController.popBackStack() },
                onProductClick = openProduct,
                snackbarHostState = snackbarHostState,
            )
        }

        composable(
            route = Screen.Collection.route,
            arguments = listOf(navArgument(Screen.Collection.ARG_TYPE) { type = NavType.StringType }),
        ) { entry ->
            val type = entry.arguments?.getString(Screen.Collection.ARG_TYPE).orEmpty()
            val title = when (type) {
                "deals" -> stringResource(R.string.home_deals)
                "new" -> stringResource(R.string.home_new)
                "recommended" -> stringResource(R.string.home_recommended)
                "popular" -> stringResource(R.string.home_popular)
                else -> stringResource(R.string.catalog_title)
            }
            CatalogRoute(
                title = title,
                contentPadding = contentPadding,
                onBackClick = { navController.popBackStack() },
                onProductClick = openProduct,
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Search.route) {
            SearchRoute(
                initialQuery = "",
                contentPadding = contentPadding,
                onBackClick = { navController.popBackStack() },
                onProductClick = openProduct,
                snackbarHostState = snackbarHostState,
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) {
            ProductDetailRoute(
                productId = it.arguments?.getString("productId").orEmpty(),
                onBackClick = { navController.popBackStack() },
                onProductClick = openProduct,
                onCartClick = { openTab(Screen.Cart.route) },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Cart.route) {
            CartRoute(
                contentPadding = contentPadding,
                onProductClick = openProduct,
                onCheckout = { navController.navigateSingleTop(Screen.Checkout.route) },
                onStartShopping = { openTab(Screen.Catalog.route) },
                snackbarHostState = snackbarHostState,
                isOnline = isOnline,
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutRoute(
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                },
                onAddAddress = { navController.navigateSingleTop(Screen.Addresses.route) },
            )
        }

        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            OrderSuccessRoute(
                orderId = it.arguments?.getString("orderId").orEmpty(),
                onTrackOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = false }
                    }
                },
                onHomeClick = { openTab(Screen.Home.route) },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Orders.route) {
            OrdersRoute(
                contentPadding = contentPadding,
                onBackClick = null,
                onOrderClick = { orderId ->
                    navController.navigateSingleTop(Screen.OrderDetail.createRoute(orderId))
                },
                onStartShopping = { openTab(Screen.Catalog.route) },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            OrderDetailRoute(
                onBackClick = { navController.popBackStack() },
                onProductClick = openProduct,
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Profile.route) {
            ProfileRoute(
                contentPadding = contentPadding,
                onLoginClick = { navController.navigateSingleTop(Screen.Auth.route) },
                onEditProfile = { navController.navigateSingleTop(Screen.EditProfile.route) },
                onOrdersClick = { openTab(Screen.Orders.route) },
                onAddressesClick = { navController.navigateSingleTop(Screen.Addresses.route) },
                onFavoritesClick = { navController.navigateSingleTop(Screen.Favorites.route) },
                onHelpClick = {
                    navController.navigateSingleTop(
                        Screen.Info.createRoute(Screen.Info.TYPE_HELP),
                    )
                },
                onPrivacyClick = {
                    navController.navigateSingleTop(
                        Screen.Info.createRoute(Screen.Info.TYPE_PRIVACY),
                    )
                },
                onTermsClick = {
                    navController.navigateSingleTop(
                        Screen.Info.createRoute(Screen.Info.TYPE_TERMS),
                    )
                },
                onSettingsClick = { navController.navigateSingleTop(Screen.Settings.route) },
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileRoute(
                onBackClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Auth.route) {
            AuthRoute(
                onBackClick = { navController.popBackStack() },
                onAuthenticated = {
                    navController.popBackStack()
                },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Addresses.route) {
            AddressesRoute(
                onBackClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState,
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesRoute(
                onBackClick = { navController.popBackStack() },
                onProductClick = openProduct,
                snackbarHostState = snackbarHostState,
            )
        }

        composable(
            route = Screen.Info.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType }),
        ) {
            InfoRoute(
                type = it.arguments?.getString("type").orEmpty(),
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsRoute(
                onBackClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

/** Bir xil manzilga takror bosilganda yangi nusxa ochilmaydi. */
private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}
