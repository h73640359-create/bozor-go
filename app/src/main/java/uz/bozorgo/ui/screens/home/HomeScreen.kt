package uz.bozorgo.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.domain.model.Banner
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.model.Product
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.LoadingState
import uz.bozorgo.ui.components.ProductImage
import uz.bozorgo.ui.components.ProductRow
import uz.bozorgo.ui.components.ProductCard
import uz.bozorgo.ui.components.SectionHeader
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun HomeRoute(
    onNavigateToSearch: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCollection: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToOrders: () -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val addedMessage = stringResource(R.string.product_added_to_cart)
    val scope = rememberCoroutineScope()
    val favoriteMessage = stringResource(R.string.product_added_to_favorites)

    HomeScreen(
        state = state,
        modifier = modifier.padding(contentPadding),

        onSearchClick = onNavigateToSearch,
        onAddressClick = onNavigateToAddresses,
        onCategoryClick = onNavigateToCategory,
        onProductClick = onNavigateToProduct,
        onSeeAllClick = onNavigateToCollection,
        onFavoritesClick = onNavigateToFavorites,
        onOrdersClick = onNavigateToOrders,
        onAddToCart = { product ->
            viewModel.addToCart(product)
            scope.launch { snackbarHostState.showSnackbar(addedMessage) }
        },
        onToggleFavorite = { productId ->
            viewModel.toggleFavorite(productId)
            scope.launch { snackbarHostState.showSnackbar(favoriteMessage) }
        },
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onSearchClick: () -> Unit,
    onAddressClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        HomeTopBar(
            addressText = state.addressText,
            onAddressClick = onAddressClick,
            onFavoritesClick = onFavoritesClick,
            onOrdersClick = onOrdersClick,
        )

        SearchField(
            onSearchClick = onSearchClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when {
            state.isLoading && state.sections == null -> LoadingState()
            state.error != null && state.sections == null -> ErrorState(
                message = state.error,
                onRetry = onRetry,
            )

            state.sections == null -> EmptyState(
                title = stringResource(R.string.error_empty_title),
                subtitle = stringResource(R.string.common_loading),
                actionLabel = stringResource(R.string.common_retry),
                onAction = onRetry,
            )

            else -> {
                val sections = state.sections
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    if (state.banners.isNotEmpty()) {
                        item {
                            BannersCarousel(
                                banners = state.banners,
                                onBannerClick = { banner ->
                                    if (banner.actionType == "category") onCategoryClick(banner.actionValue)
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }

                    if (state.categories.isNotEmpty()) {
                        item {
                            SectionHeader(title = stringResource(R.string.home_categories))
                            CategoriesRow(
                                categories = state.categories,
                                onCategoryClick = onCategoryClick,
                            )
                        }
                    }

                    if (sections!!.deals.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.home_deals),
                                actionLabel = stringResource(R.string.common_see_all),
                                onActionClick = { onSeeAllClick(COLLECTION_DEALS) },
                            )
                            ProductRow(
                                products = sections.deals,
                                favoriteIds = state.favoriteIds,
                                onProductClick = onProductClick,
                                onAddToCart = onAddToCart,
                                onToggleFavorite = onToggleFavorite,
                            )
                        }
                    }

                    if (sections.popular.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.home_popular),
                                actionLabel = stringResource(R.string.common_see_all),
                                onActionClick = { onSeeAllClick(COLLECTION_POPULAR) },
                            )
                            ProductRow(
                                products = sections.popular,
                                favoriteIds = state.favoriteIds,
                                onProductClick = onProductClick,
                                onAddToCart = onAddToCart,
                                onToggleFavorite = onToggleFavorite,
                            )
                        }
                    }

                    if (sections.newArrivals.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.home_new),
                                actionLabel = stringResource(R.string.common_see_all),
                                onActionClick = { onSeeAllClick(COLLECTION_NEW) },
                            )
                            ProductRow(
                                products = sections.newArrivals,
                                favoriteIds = state.favoriteIds,
                                onProductClick = onProductClick,
                                onAddToCart = onAddToCart,
                                onToggleFavorite = onToggleFavorite,
                            )
                        }
                    }

                    if (sections.daily.isNotEmpty()) {
                        item {
                            SectionHeader(title = stringResource(R.string.home_daily))
                        }
                        items(sections.daily.chunked(2), key = { it.first().id }) { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                pair.forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductCard(
                                            product = product,
                                            onClick = { onProductClick(product.id) },
                                            onAddToCart = { onAddToCart(product) },
                                            isFavorite = state.favoriteIds.contains(product.id),
                                            onToggleFavorite = { onToggleFavorite(product.id) },
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (sections.recommended.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.home_recommended),
                                actionLabel = stringResource(R.string.common_see_all),
                                onActionClick = { onSeeAllClick(COLLECTION_RECOMMENDED) },
                            )
                        }
                        items(sections.recommended.chunked(2), key = { "rec-" + it.first().id }) { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                pair.forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductCard(
                                            product = product,
                                            onClick = { onProductClick(product.id) },
                                            onAddToCart = { onAddToCart(product) },
                                            isFavorite = state.favoriteIds.contains(product.id),
                                            onToggleFavorite = { onToggleFavorite(product.id) },
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    addressText: String,
    onAddressClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onOrdersClick: () -> Unit,
) {
    Surface(shadowElevation = 1.dp, color = Color.White) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BozorGoColors.BrandGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bozorgo_logo),
                        contentDescription = stringResource(R.string.app_name),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        color = BozorGoColors.TextPrimary,
                    )
                    Row(
                        modifier = Modifier.clickable(onClick = onAddressClick),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = BozorGoColors.TextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = addressText.ifBlank { stringResource(R.string.home_change_address) },
                            style = MaterialTheme.typography.bodySmall,
                            color = BozorGoColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = BozorGoColors.TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                IconButton(onClick = onFavoritesClick) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = stringResource(R.string.home_favorites),
                        tint = BozorGoColors.TextPrimary,
                    )
                }
                IconButton(onClick = onOrdersClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.profile_notifications),
                        tint = BozorGoColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSearchClick() },
            enabled = false,
            readOnly = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.home_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = BozorGoColors.TextPrimary,
                disabledBorderColor = BozorGoColors.Outline,
                disabledLeadingIconColor = BozorGoColors.TextSecondary,
                disabledPlaceholderColor = BozorGoColors.TextTertiary,
                disabledContainerColor = Color.White,
            ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannersCarousel(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(6_000)
                val next = (pagerState.currentPage + 1) % banners.size
                runCatching { pagerState.animateScrollToPage(next) }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            BannerCard(banner = banners[page], onClick = { onBannerClick(banners[page]) })
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (index == pagerState.currentPage) 16.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                BozorGoColors.Outline
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun BannerCard(
    banner: Banner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val start = remember(banner.startColorHex) { parseColorSafe(banner.startColorHex) }
    val end = remember(banner.endColorHex) { parseColorSafe(banner.endColorHex) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(start, end)))
                .padding(18.dp),
        ) {
            // Fon: banner rasmi (APK ichidagi asset). Rasm bo'lmasa gradient qoladi.
            if (banner.imageAssetPath.isNotBlank()) {
                ProductImage(
                    model = uz.bozorgo.core.common.Constants.ASSET_IMAGE_PREFIX + banner.imageAssetPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(210.dp),
            ) {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = banner.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    contentColor = BozorGoColors.TextPrimary,
                ) {
                    Text(
                        text = stringResource(R.string.common_see_all),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesRow(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryChip(category = category, onClick = { onCategoryClick(category.id) })
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    onClick: () -> Unit,
) {
    val tint = remember(category.colorHex) { parseColorSafe(category.colorHex) }
    Column(
        modifier = Modifier
            .width(78.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            ProductImage(
                model = category.imageModel(),
                contentDescription = category.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = BozorGoColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** HEX rangni xavfsiz parse qilish (#RRGGBB yoki #AARRGGBB). */
internal fun parseColorSafe(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(BozorGoColors.BrandGreen)

/** To'plam turlari ("Barchasini ko'rish" ekrani uchun). */
const val COLLECTION_POPULAR = "popular"
const val COLLECTION_NEW = "new"
const val COLLECTION_DEALS = "deals"
const val COLLECTION_RECOMMENDED = "recommended"
