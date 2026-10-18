package uz.bozorgo.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.repository.OrderRepository
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val orders: List<Order> = emptyList(),
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    val uiState: StateFlow<OrdersUiState> = orderRepository.observeOrders()
        .map { orders -> OrdersUiState(isLoading = false, orders = orders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())

    init {
        // Internet paydo bo'lganda yuborilmagan buyurtmalarni serverga yozish
        viewModelScope.launch { orderRepository.syncPendingOrders() }
    }
}
