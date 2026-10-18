package uz.bozorgo.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.domain.model.Product
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ProductCard
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun FavoritesRoute(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val addedMessage = stringResource(R.string.product_added_to_cart)
    val scope = rememberCoroutineScope()

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
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        if (state.products.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.favorites_empty_title),
                subtitle = stringResource(R.string.favorites_empty_subtitle),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 158.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = {
                            viewModel.addToCart(product)
                            scope.launch { snackbarHostState.showSnackbar(addedMessage) }
                        },
                        isFavorite = true,
                        onToggleFavorite = { viewModel.remove(product.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesSpacer() {
    Spacer(Modifier.width(8.dp))
}
