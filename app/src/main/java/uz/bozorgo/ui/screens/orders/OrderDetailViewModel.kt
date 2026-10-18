package uz.bozorgo.ui.screens.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.repository.OrderRepository
import javax.inject.Inject

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val error: String? = null,
    val isCancelling: Boolean = false,
    val showCancelDialog: Boolean = false,
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: ""

    private val order = orderRepository.observeOrder(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _error = MutableStateFlow<String?>(null)
    private val _isCancelling = MutableStateFlow(false)
    private val _showCancelDialog = MutableStateFlow(false)

    val uiState: StateFlow<OrderDetailUiState> = combine(order, _error) { order, error ->
        OrderDetailUiState(isLoading = order == null, order = order, error = error)
    }
        .combine(_isCancelling) { state, cancelling -> state.copy(isCancelling = cancelling) }
        .combine(_showCancelDialog) { state, show -> state.copy(showCancelDialog = show) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderDetailUiState())

    fun requestCancel() {
        _showCancelDialog.value = true
    }

    fun dismissCancel() {
        _showCancelDialog.value = false
    }

    fun confirmCancel() {
        viewModelScope.launch {
            _isCancelling.value = true
            _showCancelDialog.value = false
            val result = orderRepository.cancelOrder(orderId)
            if (result is uz.bozorgo.core.common.Resource.Error) {
                _error.value = result.message
            }
            _isCancelling.value = false
        }
    }
}
