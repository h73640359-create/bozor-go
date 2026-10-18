package uz.bozorgo.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.domain.model.Address
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.PaymentMethod
import uz.bozorgo.ui.components.ErrorState
import uz.bozorgo.ui.components.ProductImage
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun CheckoutRoute(
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onAddAddress: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    CheckoutScreen(
        state = state,
        modifier = modifier,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onCommentChange = viewModel::onCommentChange,
        onAddressSelect = viewModel::onAddressSelect,
        onAddAddress = onAddAddress,
        onDeliveryMethodChange = viewModel::onDeliveryMethodChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onPlaceOrder = { viewModel.placeOrder(onOrderPlaced) },
    )
}

@Composable
private fun CheckoutScreen(
    state: CheckoutUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onAddressSelect: (String) -> Unit,
    onAddAddress: () -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        text = stringResource(R.string.checkout_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            if (state.summary.itemsCount == 0) {
                ErrorState(message = stringResource(R.string.cart_empty_title), onRetry = null)
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    SectionCard(title = stringResource(R.string.checkout_recipient)) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = onNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.checkout_name_hint)) },
                            isError = state.nameError != null,
                            supportingText = if (state.nameError != null) {
                                { Text(stringResource(state.nameError!!)) }
                            } else {
                                null
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = onPhoneChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.checkout_phone_hint)) },
                            isError = state.phoneError != null,
                            supportingText = if (state.phoneError != null) {
                                { Text(stringResource(state.phoneError!!)) }
                            } else {
                                null
                            },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    SectionCard(title = stringResource(R.string.checkout_address)) {
                        if (state.addresses.isEmpty()) {
                            Text(
                                text = stringResource(R.string.address_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BozorGoColors.TextSecondary,
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onAddAddress) {
                                Text(stringResource(R.string.checkout_add_address))
                            }
                        } else {
                            state.addresses.forEach { address ->
                                AddressOption(
                                    address = address,
                                    selected = address.id == state.selectedAddressId,
                                    onSelect = { onAddressSelect(address.id) },
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.checkout_add_address),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable(onClick = onAddAddress),
                            )
                        }
                        if (state.addressError != null) {
                            Text(
                                text = stringResource(state.addressError),
                                style = MaterialTheme.typography.labelSmall,
                                color = BozorGoColors.Error,
                            )
                        }
                    }

                    SectionCard(title = stringResource(R.string.checkout_delivery_method)) {
                        MethodOption(
                            title = stringResource(R.string.checkout_delivery_standard),
                            subtitle = MoneyFormatter.formatUzs(state.deliveryFeeFor(DeliveryMethod.STANDARD)),
                            selected = state.deliveryMethod == DeliveryMethod.STANDARD,
                            onSelect = { onDeliveryMethodChange(DeliveryMethod.STANDARD) },
                        )
                        MethodOption(
                            title = stringResource(R.string.checkout_delivery_express),
                            subtitle = MoneyFormatter.formatUzs(state.deliveryFeeFor(DeliveryMethod.EXPRESS)),
                            selected = state.deliveryMethod == DeliveryMethod.EXPRESS,
                            onSelect = { onDeliveryMethodChange(DeliveryMethod.EXPRESS) },
                        )
                        MethodOption(
                            title = stringResource(R.string.checkout_delivery_pickup),
                            subtitle = stringResource(R.string.common_free),
                            selected = state.deliveryMethod == DeliveryMethod.PICKUP,
                            onSelect = { onDeliveryMethodChange(DeliveryMethod.PICKUP) },
                        )
                    }

                    SectionCard(title = stringResource(R.string.checkout_payment)) {
                        MethodOption(
                            title = stringResource(R.string.checkout_pay_cash),
                            subtitle = null,
                            selected = state.paymentMethod == PaymentMethod.CASH,
                            onSelect = { onPaymentMethodChange(PaymentMethod.CASH) },
                        )
                        MethodOption(
                            title = stringResource(R.string.checkout_pay_card),
                            subtitle = stringResource(R.string.help_faq_3_text).take(60),
                            selected = state.paymentMethod == PaymentMethod.CARD,
                            onSelect = { onPaymentMethodChange(PaymentMethod.CARD) },
                        )
                    }

                    SectionCard(title = stringResource(R.string.checkout_comment)) {
                        OutlinedTextField(
                            value = state.comment,
                            onValueChange = onCommentChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.checkout_comment_hint)) },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    SectionCard(title = stringResource(R.string.checkout_summary)) {
                        // Buyurtma tarkibidagi mahsulotlar rasmlari.
                        if (state.items.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                state.items.take(5).forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BozorGoColors.SurfaceVariant),
                                    ) {
                                        ProductImage(
                                            model = item.product.imageModel(),
                                            fallbackModel = item.product.fallbackImageModel(),
                                            contentDescription = item.product.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                }
                                if (state.items.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BozorGoColors.SurfaceVariant),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "+${state.items.size - 5}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = BozorGoColors.TextSecondary,
                                        )
                                    }
                                }
                            }
                        }
                        SummaryLine(
                            label = stringResource(R.string.cart_items_sum),
                            value = MoneyFormatter.formatUzs(state.summary.itemsTotal),
                        )
                        if (state.summary.discount > 0) {
                            SummaryLine(
                                label = stringResource(R.string.cart_discount),
                                value = "-${MoneyFormatter.formatUzs(state.summary.discount)}",
                            )
                        }
                        SummaryLine(
                            label = stringResource(R.string.cart_delivery),
                            value = if (state.summary.deliveryFee == 0.0) {
                                stringResource(R.string.common_free)
                            } else {
                                MoneyFormatter.formatUzs(state.summary.deliveryFee)
                            },
                        )
                        SummaryLine(
                            label = stringResource(R.string.cart_total),
                            value = MoneyFormatter.formatUzs(state.summary.total),
                            isTotal = true,
                        )
                    }

                    if (!state.isServerConfigured) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BozorGoColors.BrandOrangeLight,
                        ) {
                            Text(
                                text = stringResource(R.string.checkout_offline_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = BozorGoColors.TextPrimary,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = BozorGoColors.Error,
                        )
                    }

                    if (state.paymentMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BozorGoColors.BrandGreenLight,
                        ) {
                            Text(
                                text = state.paymentMessage,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(90.dp))
                }
            }
        }

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
                    onClick = onPlaceOrder,
                    enabled = !state.isPlacing,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (state.isPlacing) {
                            stringResource(R.string.common_loading)
                        } else {
                            stringResource(R.string.checkout_place_order)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = BozorGoColors.TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun AddressOption(
    address: Address,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = address.title,
                style = MaterialTheme.typography.bodyMedium,
                color = BozorGoColors.TextPrimary,
            )
            Text(
                text = address.fullAddress,
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun MethodOption(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = BozorGoColors.TextPrimary,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = BozorGoColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    isTotal: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (isTotal) MaterialTheme.colorScheme.primary else BozorGoColors.TextPrimary,
        )
    }
}
