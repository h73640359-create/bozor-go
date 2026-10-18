package uz.bozorgo.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.model.SortOption
import uz.bozorgo.domain.model.Subcategory
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.ProductCard
import uz.bozorgo.ui.components.ShimmerBox
import uz.bozorgo.ui.screens.home.parseColorSafe
import uz.bozorgo.ui.components.ProductImage
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun CatalogRoute(
    title: String,
    onBackClick: (() -> Unit)?,
    onProductClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val addedMessage = stringResource(R.string.product_added_to_cart)
    val favoriteMessage = stringResource(R.string.product_added_to_favorites)
    val scope = rememberCoroutineScope()

    CatalogScreen(
        title = title,
        state = state,
        modifier = modifier.padding(contentPadding),

        onBackClick = onBackClick,
        onSubcategoryClick = viewModel::selectSubcategory,
        onSortChange = viewModel::setSort,
        onFilterChange = viewModel::updateFilter,
        onPriceRangeChange = viewModel::setPriceRange,
        onBrandToggle = viewModel::toggleBrand,
        onResetFilters = viewModel::resetFilters,
        onProductClick = onProductClick,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::refresh,
        onAddToCart = { product ->
            viewModel.addToCart(product)
            scope.launch { snackbarHostState.showSnackbar(addedMessage) }
        },
        onToggleFavorite = { productId ->
            viewModel.toggleFavorite(productId)
            scope.launch { snackbarHostState.showSnackbar(favoriteMessage) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogScreen(
    title: String,
    state: CatalogUiState,
    onBackClick: (() -> Unit)?,
    onSubcategoryClick: (String?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (Boolean, Boolean) -> Unit,
    onPriceRangeChange: (Double, Double) -> Unit,
    onBrandToggle: (String) -> Unit,
    onResetFilters: () -> Unit,
    onProductClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    var showFilters by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.products.size - 6 && state.products.size < state.totalCount
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    Column(modifier = modifier.fillMaxSize()) {
        CatalogTopBar(
            title = title,
            onBackClick = onBackClick,
        )

        if (state.subcategories.isNotEmpty()) {
            SubcategoriesRow(
                subcategories = state.subcategories,
                selectedId = state.filter.subcategoryId,
                onSelect = onSubcategoryClick,
                totalCount = state.totalCount,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.catalog_products_count, state.totalCount),
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
                modifier = Modifier.weight(1f),
            )
            SortChip(selected = state.filter.sort, onClick = { showSort = true })
            Spacer(Modifier.width(6.dp))
            FilterChip(
                selected = state.filter.onlyDiscount || state.filter.inStockOnly ||
                    state.filter.minPrice != null || state.filter.brands.isNotEmpty(),
                onClick = { showFilters = true },
                label = { Text(stringResource(R.string.catalog_filters)) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }

        when {
            state.isLoading && state.products.isEmpty() -> CatalogLoading()
            state.error != null && state.products.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = onRetry,
            )

            state.products.isEmpty() -> EmptyState(
                title = stringResource(R.string.catalog_empty),
                subtitle = stringResource(R.string.common_reset),
                actionLabel = stringResource(R.string.common_reset),
                onAction = onResetFilters,
            )

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 158.dp),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            onAddToCart = { onAddToCart(product) },
                            isFavorite = state.favoriteIds.contains(product.id),
                            onToggleFavorite = { onToggleFavorite(product.id) },
                        )
                    }
                    if (state.products.size < state.totalCount) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                TextButton(onClick = onLoadMore) {
                                    Text(stringResource(R.string.catalog_load_more))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
        ) {
            FilterSheetContent(
                state = state,
                onFilterChange = onFilterChange,
                onPriceRangeChange = onPriceRangeChange,
                onBrandToggle = onBrandToggle,
                onReset = onResetFilters,
                onApply = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showFilters = false
                    }
                },
            )
        }
    }

    if (showSort) {
        ModalBottomSheet(onDismissRequest = { showSort = false }) {
            SortSheetContent(
                selected = state.filter.sort,
                onSelected = {
                    onSortChange(it)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showSort = false
                    }
                },
            )
        }
    }
}

@Composable
private fun CatalogTopBar(
    title: String,
    onBackClick: (() -> Unit)?,
) {
    Surface(shadowElevation = 1.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = BozorGoColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SubcategoriesRow(
    subcategories: List<Subcategory>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    totalCount: Int,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.common_all)) },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                ),
            )
        }
        items(subcategories, key = { it.id }) { sub ->
            FilterChip(
                selected = selectedId == sub.id,
                onClick = { onSelect(sub.id) },
                label = { Text("${sub.name} (${sub.productCount})") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
private fun SortChip(
    selected: SortOption,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(sortLabel(selected)) },
        leadingIcon = {
            Icon(Icons.Outlined.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        shape = RoundedCornerShape(10.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White,
            labelColor = BozorGoColors.TextPrimary,
        ),
    )
}

@Composable
private fun sortLabel(sort: SortOption): String = when (sort) {
    SortOption.POPULAR -> stringResource(R.string.catalog_sort_popular)
    SortOption.PRICE_ASC -> stringResource(R.string.catalog_sort_price_asc)
    SortOption.PRICE_DESC -> stringResource(R.string.catalog_sort_price_desc)
    SortOption.NAME -> stringResource(R.string.catalog_sort_name)
    SortOption.NEWEST -> stringResource(R.string.catalog_sort_new)
    SortOption.DISCOUNT -> stringResource(R.string.catalog_sort_discount)
}

@Composable
private fun CatalogLoading(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        repeat(6) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShimmerBox(modifier = Modifier.weight(1f).height(210.dp))
                ShimmerBox(modifier = Modifier.weight(1f).height(210.dp))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheetContent(
    state: CatalogUiState,
    onFilterChange: (Boolean, Boolean) -> Unit,
    onPriceRangeChange: (Double, Double) -> Unit,
    onBrandToggle: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    var onlyDiscount by remember(state.filter.onlyDiscount) { mutableStateOf(state.filter.onlyDiscount) }
    var inStockOnly by remember(state.filter.inStockOnly) { mutableStateOf(state.filter.inStockOnly) }
    val range = state.priceRange
    var sliderValues by remember(state.filter.minPrice, state.filter.maxPrice, range) {
        mutableStateOf(
            (state.filter.minPrice ?: range.start).toFloat()..
                (state.filter.maxPrice ?: range.endInclusive).toFloat(),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.catalog_filters),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.catalog_price_range),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = "${
                MoneyFormatter.formatUzs(sliderValues.start.toDouble())
            } — ${MoneyFormatter.formatUzs(sliderValues.endInclusive.toDouble())}",
            style = MaterialTheme.typography.bodySmall,
            color = BozorGoColors.TextSecondary,
        )
        RangeSlider(
            value = sliderValues,
            onValueChange = { sliderValues = it },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            onValueChangeFinished = {
                onPriceRangeChange(sliderValues.start.toDouble(), sliderValues.endInclusive.toDouble())
            },
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = onlyDiscount, onCheckedChange = {
                onlyDiscount = it
                onFilterChange(onlyDiscount, inStockOnly)
            })
            Text(stringResource(R.string.catalog_only_discount))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = inStockOnly, onCheckedChange = {
                inStockOnly = it
                onFilterChange(onlyDiscount, inStockOnly)
            })
            Text(stringResource(R.string.catalog_in_stock_only))
        }

        if (state.brands.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.product_brand, "").trim().removeSuffix(":"),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.brands) { brand ->
                    FilterChip(
                        selected = state.filter.brands.contains(brand),
                        onClick = { onBrandToggle(brand) },
                        label = { Text(brand) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.common_reset))
            }
            Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.common_apply))
            }
        }
    }
}

@Composable
private fun SortSheetContent(
    selected: SortOption,
    onSelected: (SortOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.catalog_sort),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        SortOption.entries.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelected(option) }
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sortLabel(option),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (option == selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        BozorGoColors.TextPrimary
                    },
                    modifier = Modifier.weight(1f),
                )
                if (option == selected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}

/** Katalog kategoriya kartasi (CatalogScreen ichida ishlatiladi). */
@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = remember(category.colorHex) { parseColorSafe(category.colorHex) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Kategoriya rasmi: APK ichidagi asset, 16:10 nisbatda (kesilmaydi).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(tint.copy(alpha = 0.10f)),
            ) {
                ProductImage(
                    model = category.imageModel(),
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = BozorGoColors.TextPrimary,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.catalog_products_count, category.productCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.TextSecondary,
                )
            }
        }
    }
}
