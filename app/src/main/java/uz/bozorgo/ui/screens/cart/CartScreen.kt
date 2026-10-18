package uz.bozorgo.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.Product
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.LoadingState
import uz.bozorgo.ui.components.ProductImage
import uz.bozorgo.ui.components.QuantityStepper
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun CartRoute(
    onProductClick: (String) -> Unit,
    onCheckout: () -> Unit,
    onStartShopping: () -> Unit,
    snackbarHostState: SnackbarHostState,
    isOnline: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    CartScreen(
        state = state,
        isOnline = isOnline,
        modifier = modifier.padding(contentPadding),

        onIncrease = viewModel::increase,
        onDecrease = viewModel::decrease,
        onRemove = viewModel::remove,
        onClear = viewModel::requestClear,
        onConfirmClear = viewModel::clear,
        onDismissClear = viewModel::dismissClear,
        onProductClick = onProductClick,
        onCheckout = onCheckout,
        onStartShopping = onStartShopping,
    )
}

@Composable
private fun CartScreen(
    state: CartUiState,
    isOnline: Boolean,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    onClear: () -> Unit,
    onConfirmClear: () -> Unit,
    onDismissClear: () -> Unit,
    onProductClick: (String) -> Unit,
    onCheckout: () -> Unit,
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 1.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.cart_title),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        if (state.items.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.cart_items, state.summary.itemsCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = BozorGoColors.TextSecondary,
                            )
                        }
                    }
                    if (state.items.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text(stringResource(R.string.cart_clear))
                        }
                    }
                }
            }

            if (!isOnline) {
                OfflineHint()
            }

            when {
                state.error != null -> ErrorState(message = state.error, onRetry = null)
                state.items.isEmpty() -> EmptyState(
                    title = stringResource(R.string.cart_empty_title),
                    subtitle = stringResource(R.string.cart_empty_subtitle),
                    actionLabel = stringResource(R.string.cart_go_shopping),
                    onAction = onStartShopping,
                )

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 10.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.items, key = { it.product.id + (it.selectedOption ?: "") }) { item ->
                            CartItemRow(
                                item = item,
                                onIncrease = { onIncrease(item) },
                                onDecrease = { onDecrease(item) },
                                onRemove = { onRemove(item) },
                                onClick = { onProductClick(item.product.id) },
                            )
                        }
                        item {
                            SummaryCard(summary = state.summary)
                        }
                        item { Spacer(Modifier.height(90.dp)) }
                    }
                }
            }
        }

        if (state.items.isNotEmpty()) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.cart_total),
                            style = MaterialTheme.typography.bodySmall,
                            color = BozorGoColors.TextSecondary,
                        )
                        Text(
                            text = MoneyFormatter.formatUzs(state.summary.total),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.cart_checkout))
                    }
                }
            }
        }
    }

    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = onDismissClear,
            title = { Text(stringResource(R.string.cart_clear)) },
            text = { Text(stringResource(R.string.cart_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = onConfirmClear) { Text(stringResource(R.string.common_yes)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissClear) { Text(stringResource(R.string.common_no)) }
            },
        )
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BozorGoColors.SurfaceVariant),
            ) {
                ProductImage(
                    model = item.product.imageModelOrNull(),
                    contentDescription = item.product.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BozorGoColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.product.unitLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = BozorGoColors.TextTertiary,
                )
                if (item.selectedOption != null) {
                    Text(
                        text = item.selectedOption,
                        style = MaterialTheme.typography.labelSmall,
                        color = BozorGoColors.TextSecondary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = MoneyFormatter.formatUzs(item.product.effectivePrice),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    QuantityStepper(
                        quantity = item.quantity,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        compact = true,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = MoneyFormatter.formatUzs(item.lineTotal),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    if (item.lineDiscount > 0) {
                        Text(
                            text = "-${MoneyFormatter.formatUzs(item.lineDiscount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BozorGoColors.Error,
                        )
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.cart_remove_item),
                            tint = BozorGoColors.TextTertiary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (!item.product.inStock) {
                    Text(
                        text = stringResource(R.string.product_out_of_stock),
                        style = MaterialTheme.typography.labelSmall,
                        color = BozorGoColors.Error,
                    )
                } else if (item.quantity >= item.product.stock) {
                    Text(
                        text = stringResource(R.string.product_left_count, item.product.stock),
                        style = MaterialTheme.typography.labelSmall,
                        color = BozorGoColors.BrandOrange,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: CartSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SummaryRow(
                label = stringResource(R.string.cart_items_sum),
                value = MoneyFormatter.formatUzs(summary.itemsTotal),
            )
            if (summary.discount > 0) {
                SummaryRow(
                    label = stringResource(R.string.cart_discount),
                    value = "-${MoneyFormatter.formatUzs(summary.discount)}",
                    valueColor = BozorGoColors.Error,
                )
            }
            SummaryRow(
                label = stringResource(R.string.cart_delivery),
                value = if (summary.deliveryFee == 0.0) {
                    stringResource(R.string.common_free)
                } else {
                    MoneyFormatter.formatUzs(summary.deliveryFee)
                },
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.cart_total),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = MoneyFormatter.formatUzs(summary.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (summary.deliveryFee > 0) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BozorGoColors.BrandOrangeLight,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = BozorGoColors.BrandOrange,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                R.string.cart_free_delivery_hint,
                                MoneyFormatter.formatUzs(
                                    uz.bozorgo.core.common.Constants.FREE_DELIVERY_THRESHOLD - summary.itemsTotal,
                                ),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = BozorGoColors.TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = BozorGoColors.TextPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
        )
    }
}

@Composable
private fun OfflineHint() {
    Surface(color = BozorGoColors.BrandOrangeLight) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.error_network_text),
                style = MaterialTheme.typography.labelSmall,
                color = BozorGoColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
