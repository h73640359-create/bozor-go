package uz.bozorgo.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun OrderSuccessRoute(
    orderId: String,
    onTrackOrderClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: OrderSuccessViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    OrderSuccessScreen(
        state = state,
        modifier = modifier,
        onTrackOrderClick = { state.order?.id?.let(onTrackOrderClick) },
        onHomeClick = onHomeClick,
    )
}

@Composable
private fun OrderSuccessScreen(
    state: OrderSuccessUiState,
    onTrackOrderClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = BozorGoColors.Success,
            modifier = Modifier.size(84.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.order_success_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.order_success_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextSecondary,
            textAlign = TextAlign.Center,
        )

        state.order?.let { order ->
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoLine(
                        label = stringResource(R.string.order_success_number),
                        value = order.orderNumber,
                    )
                    InfoLine(
                        label = stringResource(R.string.order_success_total),
                        value = MoneyFormatter.formatUzs(order.total),
                    )
                    InfoLine(
                        label = stringResource(R.string.orders_address),
                        value = order.deliveryAddress.fullAddress,
                    )
                    InfoLine(
                        label = stringResource(R.string.orders_payment),
                        value = if (order.paymentMethod.key == "cash") {
                            stringResource(R.string.checkout_pay_cash)
                        } else {
                            stringResource(R.string.checkout_pay_card)
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onTrackOrderClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.order_success_track))
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.order_success_home))
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = BozorGoColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextPrimary,
        )
    }
}
