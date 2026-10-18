package uz.bozorgo.ui.screens.search

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.domain.model.Product
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.ProductCard
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun SearchRoute(
    initialQuery: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val addedMessage = stringResource(R.string.product_added_to_cart)
    val favoriteMessage = stringResource(R.string.product_added_to_favorites)
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) viewModel.onQueryChange(initialQuery)
    }

    SearchScreen(
        state = state,
        modifier = modifier.padding(contentPadding),

        onQueryChange = viewModel::onQueryChange,
        onSearchSubmit = viewModel::submit,
        onHistoryClick = viewModel::onQueryChange,
        onHistoryRemove = viewModel::removeHistoryItem,
        onClearHistory = viewModel::clearHistory,
        onBackClick = onBackClick,
        onProductClick = onProductClick,
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

@Composable
private fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onHistoryRemove: (String) -> Unit,
    onClearHistory: () -> Unit,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                TextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = BozorGoColors.SurfaceVariant,
                        unfocusedContainerColor = BozorGoColors.SurfaceVariant,
                    ),
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.common_reset),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                )
                IconButton(onClick = { onSearchSubmit(state.query) }) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.common_search),
                    )
                }
            }
        }

        when {
            state.query.isBlank() -> SearchHistory(
                history = state.history,
                popular = state.popularQueries,
                onHistoryClick = onHistoryClick,
                onHistoryRemove = onHistoryRemove,
                onClearHistory = onClearHistory,
            )

            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.common_loading))
            }

            state.error != null -> ErrorState(message = state.error, onRetry = { onSearchSubmit(state.query) })

            state.results.isEmpty() -> EmptyState(
                title = stringResource(R.string.search_not_found_title),
                subtitle = stringResource(R.string.search_not_found_subtitle),
            )

            else -> Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.search_results_count, state.totalCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.results.chunked(2), key = { it.first().id }) { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { product ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCard(
                                        product = product,
                                        onClick = {
                                            onSearchSubmit(state.query)
                                            onProductClick(product.id)
                                        },
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

@Composable
private fun SearchHistory(
    history: List<String>,
    popular: List<String>,
    onHistoryClick: (String) -> Unit,
    onHistoryRemove: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.search_history),
                    style = MaterialTheme.typography.titleSmall,
                    color = BozorGoColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearHistory) {
                    Text(stringResource(R.string.search_clear_history))
                }
            }
            LazyColumn {
                items(history, key = { it }) { query ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHistoryClick(query) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = BozorGoColors.TextTertiary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = query,
                            style = MaterialTheme.typography.bodyLarge,
                            color = BozorGoColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = { onHistoryRemove(query) }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.common_delete),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        } else {
            EmptyState(
                title = stringResource(R.string.search_empty_history),
                subtitle = stringResource(R.string.search_hint),
            )
        }

        if (popular.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.search_popular),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(popular) { query ->
                    AssistChip(
                        onClick = { onHistoryClick(query) },
                        label = { Text(query) },
                        shape = RoundedCornerShape(10.dp),
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White),
                    )
                }
            }
        }
    }
}
