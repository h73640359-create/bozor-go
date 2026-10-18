package uz.bozorgo.ui.screens.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.repository.OrderRepository
import javax.inject.Inject

data class OrderSuccessUiState(
    val order: Order? = null,
)

@HiltViewModel
class OrderSuccessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    orderRepository: OrderRepository,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>("orderId") ?: ""

    val uiState: StateFlow<OrderSuccessUiState> = orderRepository.observeOrder(orderId)
        .map { OrderSuccessUiState(order = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderSuccessUiState())
}
