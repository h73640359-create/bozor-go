package uz.bozorgo.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.domain.model.Product
import uz.bozorgo.ui.components.DiscountBadge
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.LoadingState
import uz.bozorgo.ui.components.ProductImage
import uz.bozorgo.ui.components.ProductRow
import uz.bozorgo.ui.components.QuantityStepper
import uz.bozorgo.ui.components.RatingView
import uz.bozorgo.ui.components.SectionHeader
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun ProductDetailRoute(
    productId: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val addedMessage = stringResource(R.string.product_added_to_cart)
    val stockMessage = stringResource(R.string.product_out_of_stock)
    val scope = rememberCoroutineScope()

    ProductDetailScreen(
        state = state,
        modifier = modifier,
        onBackClick = onBackClick,
        onProductClick = onProductClick,
        onCartClick = onCartClick,
        onOptionSelect = viewModel::selectOption,
        onQuantityChange = viewModel::setQuantity,
        onAddToCart = {
            viewModel.addToCart(
                onSuccess = { scope.launch { snackbarHostState.showSnackbar(addedMessage) } },
                onOutOfStock = { scope.launch { snackbarHostState.showSnackbar(stockMessage) } },
            )
        },
        onToggleFavorite = viewModel::toggleCurrentFavorite,
        onRetry = viewModel::retry,
        onSimilarProductClick = onProductClick,
        onAddSimilarToCart = viewModel::addToCartDirect,
        onToggleSimilarFavorite = viewModel::toggleFavorite,
    )
}

@Composable
private fun ProductDetailScreen(
    state: ProductDetailUiState,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    onSimilarProductClick: (String) -> Unit,
    onAddSimilarToCart: (Product) -> Unit,
    onToggleSimilarFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = state.product

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 1.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                    Text(
                        text = product?.name ?: stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (state.isFavorite) {
                                Icons.Outlined.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = stringResource(R.string.home_favorites),
                            tint = if (state.isFavorite) {
                                BozorGoColors.Error
                            } else {
                                BozorGoColors.TextPrimary
                            },
                        )
                    }
                    IconButton(onClick = onCartClick) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = stringResource(R.string.nav_cart),
                        )
                    }
                }
            }

            when {
                state.isLoading && product == null -> LoadingState()
                state.error != null && product == null -> ErrorState(
                    message = state.error,
                    onRetry = onRetry,
                )

                product == null -> EmptyState(
                    title = stringResource(R.string.error_empty_title),
                    subtitle = stringResource(R.string.common_loading),
                )

                else -> ProductContent(
                    product = product,
                    state = state,
                    onOptionSelect = onOptionSelect,
                    onSimilarProductClick = onSimilarProductClick,
                    onAddSimilarToCart = onAddSimilarToCart,
                    onToggleSimilarFavorite = onToggleSimilarFavorite,
                )
            }
        }

        if (product != null) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shadowElevation = 8.dp,
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuantityStepper(
                        quantity = state.quantity,
                        onIncrease = { onQuantityChange(state.quantity + 1) },
                        onDecrease = { onQuantityChange(state.quantity - 1) },
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onAddToCart,
                        enabled = product.inStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (product.inStock) {
                                stringResource(R.string.product_add_to_cart)
                            } else {
                                stringResource(R.string.product_out_of_stock)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductContent(
    product: Product,
    state: ProductDetailUiState,
    onOptionSelect: (String) -> Unit,
    onSimilarProductClick: (String) -> Unit,
    onAddSimilarToCart: (Product) -> Unit,
    onToggleSimilarFavorite: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(BozorGoColors.SurfaceVariant),
        ) {
            ProductImage(
                model = product.imageModelOrNull(),
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            if (product.hasDiscount && product.discountPercent > 0) {
                DiscountBadge(
                    percent = product.discountPercent,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = MoneyFormatter.formatUzs(product.effectivePrice),
                    style = MaterialTheme.typography.headlineSmall,
                    color = BozorGoColors.TextPrimary,
                )
                if (product.hasDiscount) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = MoneyFormatter.formatUzs(product.price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = BozorGoColors.TextTertiary,
                    )
                    Spacer(Modifier.width(6.dp))
                    DiscountBadge(percent = product.discountPercent)
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = BozorGoColors.TextPrimary,
            )

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingView(rating = product.rating, reviewCount = product.reviewCount)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (product.inStock) {
                        stringResource(R.string.product_in_stock)
                    } else {
                        stringResource(R.string.product_out_of_stock)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.inStock) BozorGoColors.Success else BozorGoColors.Error,
                )
            }

            product.options.forEach { option ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.product_select_option, option.name),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    option.values.forEach { value ->
                        FilterChip(
                            selected = state.selectedOption == value,
                            onClick = { onOptionSelect(value) },
                            label = { Text(value) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            InfoRow(label = stringResource(R.string.product_brand, product.brand))
            InfoRow(label = stringResource(R.string.product_unit_label, product.unitLabel))
            InfoRow(label = stringResource(R.string.product_category, product.categoryName))
            InfoRow(label = stringResource(R.string.product_sku, product.id))

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.product_description),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = BozorGoColors.TextSecondary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.product_demo_reviews),
                style = MaterialTheme.typography.labelSmall,
                color = BozorGoColors.TextTertiary,
            )

            Spacer(Modifier.height(16.dp))
            DeliveryInfo()

            if (state.similar.isNotEmpty()) {
                SectionHeader(title = stringResource(R.string.product_similar))
                ProductRow(
                    products = state.similar,
                    favoriteIds = state.similarFavoriteIds,
                    onProductClick = onSimilarProductClick,
                    onAddToCart = onAddSimilarToCart,
                    onToggleFavorite = onToggleSimilarFavorite,
                )
            }

            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = BozorGoColors.TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    )
}

@Composable
private fun DeliveryInfo() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BozorGoColors.SurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(
                Icons.Outlined.LocalShipping,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.product_delivery_info),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.product_delivery_info_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.TextSecondary,
                )
            }
        }
    }
}
