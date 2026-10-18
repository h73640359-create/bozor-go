package uz.bozorgo.ui.screens.orders

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.model.OrderStatus
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.LoadingState
import uz.bozorgo.ui.theme.BozorGoColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersRoute(
    onBackClick: (() -> Unit)?,
    onOrderClick: (String) -> Unit,
    onStartShopping: () -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    OrdersScreen(
        state = state,
        modifier = modifier.padding(contentPadding),

        onBackClick = onBackClick,
        onOrderClick = onOrderClick,
        onStartShopping = onStartShopping,
    )
}

@Composable
private fun OrdersScreen(
    state: OrdersUiState,
    onBackClick: (() -> Unit)?,
    onOrderClick: (String) -> Unit,
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Surface(shadowElevation = 1.dp, color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                } else {
                    Spacer(Modifier.width(14.dp))
                }
                Text(
                    text = stringResource(R.string.orders_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        when {
            state.isLoading && state.orders.isEmpty() -> LoadingState()
            state.error != null && state.orders.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = null,
            )

            state.orders.isEmpty() -> EmptyState(
                title = stringResource(R.string.orders_empty_title),
                subtitle = stringResource(R.string.orders_empty_subtitle),
                actionLabel = stringResource(R.string.cart_go_shopping),
                onAction = onStartShopping,
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(status = order.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatDate(order.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = BozorGoColors.TextTertiary,
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    text = stringResource(R.string.orders_items_count, order.itemsCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = MoneyFormatter.formatUzs(order.total),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (!order.isSynced) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.checkout_offline_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = BozorGoColors.BrandOrange,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (background, content) = statusColors(status)
    Surface(shape = RoundedCornerShape(8.dp), color = background, contentColor = content) {
        Text(
            text = statusLabel(status),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun statusColors(status: OrderStatus): Pair<Color, Color> = when (status) {
    OrderStatus.CREATED -> BozorGoColors.BrandGreenLight to BozorGoColors.BrandGreenDark
    OrderStatus.CONFIRMED -> BozorGoColors.BrandGreenLight to BozorGoColors.BrandGreenDark
    OrderStatus.PREPARING -> BozorGoColors.BrandOrangeLight to BozorGoColors.BrandOrange
    OrderStatus.COURIER -> BozorGoColors.BrandOrangeLight to BozorGoColors.BrandOrange
    OrderStatus.DELIVERING -> BozorGoColors.BrandGreenLight to BozorGoColors.BrandGreenDark
    OrderStatus.DELIVERED -> BozorGoColors.BrandGreenLight to BozorGoColors.BrandGreenDark
    OrderStatus.CANCELLED -> Color(0xFFFFE4E6) to BozorGoColors.Error
}

@Composable
fun statusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.CREATED -> stringResource(R.string.order_status_created)
    OrderStatus.CONFIRMED -> stringResource(R.string.order_status_confirmed)
    OrderStatus.PREPARING -> stringResource(R.string.order_status_preparing)
    OrderStatus.COURIER -> stringResource(R.string.order_status_courier)
    OrderStatus.DELIVERING -> stringResource(R.string.order_status_delivering)
    OrderStatus.DELIVERED -> stringResource(R.string.order_status_delivered)
    OrderStatus.CANCELLED -> stringResource(R.string.order_status_cancelled)
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

// ---------------------------------------------------------------------------
// Buyurtma tafsiloti
// ---------------------------------------------------------------------------

@Composable
fun OrderDetailRoute(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> Column(modifier = modifier.fillMaxSize()) {
            LoadingState()
        }

        state.order == null -> EmptyState(
            title = stringResource(R.string.error_empty_title),
            subtitle = stringResource(R.string.orders_title),
        )

        else -> OrderDetailScreen(
            order = state.order!!,
            isCancelling = state.isCancelling,
            error = state.error,
            showCancelDialog = state.showCancelDialog,
            modifier = modifier,
            onBackClick = onBackClick,
            onCancelClick = viewModel::requestCancel,
            onConfirmCancel = viewModel::confirmCancel,
            onDismissCancel = viewModel::dismissCancel,
        )
    }
}

@Composable
private fun OrderDetailScreen(
    order: Order,
    isCancelling: Boolean,
    error: String?,
    showCancelDialog: Boolean,
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmCancel: () -> Unit,
    onDismissCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.orders_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BozorGoColors.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(status = order.status)
            }
            Spacer(Modifier.height(12.dp))
            StatusTimeline(status = order.status)

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.orders_created_at) + ": " + formatDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
            )
            Text(
                text = stringResource(R.string.checkout_recipient) + ": " + order.recipientName,
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
            )
            Text(
                text = order.phone,
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
            )
            Text(
                text = stringResource(R.string.orders_address) + ": " + order.addressText,
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
            )
            Text(
                text = stringResource(R.string.orders_payment) + ": " + paymentLabel(order.paymentMethod),
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.orders_details),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "${item.quantity} x ${item.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = MoneyFormatter.formatUzs(item.lineTotal),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row {
                Text(
                    text = stringResource(R.string.cart_items_sum),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BozorGoColors.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(text = MoneyFormatter.formatUzs(order.itemsTotal))
            }
            Row {
                Text(
                    text = stringResource(R.string.cart_delivery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BozorGoColors.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(text = MoneyFormatter.formatUzs(order.deliveryFee))
            }
            Row {
                Text(
                    text = stringResource(R.string.cart_total),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = MoneyFormatter.formatUzs(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.Error,
                )
            }

            if (order.status.canBeCancelled) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onCancelClick,
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = BozorGoColors.Error,
                    ),
                ) {
                    Text(stringResource(R.string.orders_cancel))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = onDismissCancel,
            title = { Text(stringResource(R.string.orders_cancel)) },
            text = { Text(stringResource(R.string.orders_cancel_confirm)) },
            confirmButton = {
                TextButton(onClick = onConfirmCancel) { Text(stringResource(R.string.common_yes)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissCancel) { Text(stringResource(R.string.common_no)) }
            },
        )
    }
}

@Composable
private fun paymentLabel(method: uz.bozorgo.domain.model.PaymentMethod): String = when (method) {
    uz.bozorgo.domain.model.PaymentMethod.CASH -> stringResource(R.string.checkout_pay_cash)
    uz.bozorgo.domain.model.PaymentMethod.CARD -> stringResource(R.string.checkout_pay_card)
}

@Composable
private fun StatusTimeline(status: OrderStatus) {
    val steps = listOf(
        OrderStatus.CREATED,
        OrderStatus.CONFIRMED,
        OrderStatus.PREPARING,
        OrderStatus.DELIVERING,
        OrderStatus.DELIVERED,
    )
    Column {
        steps.forEachIndexed { index, step ->
            val isDone = status != OrderStatus.CANCELLED && status.ordinal >= step.ordinal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .padding(2.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = if (isDone) MaterialTheme.colorScheme.primary else BozorGoColors.Outline,
                        modifier = Modifier.fillMaxSize(),
                        content = {},
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = statusLabel(step),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDone) BozorGoColors.TextPrimary else BozorGoColors.TextTertiary,
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(2.dp)
                        .height(14.dp)
                        .background(
                            if (isDone) MaterialTheme.colorScheme.primary else BozorGoColors.Outline,
                        ),
                )
            }
        }
    }
}
