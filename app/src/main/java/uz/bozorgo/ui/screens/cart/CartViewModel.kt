package uz.bozorgo.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.repository.CartRepository
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val summary: CartSummary = CartSummary.empty(),
    val error: String? = null,
    val showClearDialog: Boolean = false,
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _showClearDialog = MutableStateFlow(false)

    private val items = cartRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val summary = cartRepository.observeSummary(DeliveryMethod.STANDARD)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartSummary.empty())

    val uiState: StateFlow<CartUiState> = combine(items, summary) { items, summary ->
        CartUiState(items = items, summary = summary)
    }
        .combine(_error) { state, error -> state.copy(error = error) }
        .combine(_showClearDialog) { state, show -> state.copy(showClearDialog = show) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun increase(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity >= item.product.stock) {
                _error.value = null
                return@launch
            }
            runCatching {
                cartRepository.setQuantity(
                    productId = item.product.id,
                    selectedOption = item.selectedOption,
                    quantity = item.quantity + 1,
                )
            }.onFailure { _error.value = it.message }
        }
    }

    fun decrease(item: CartItem) {
        viewModelScope.launch {
            runCatching {
                cartRepository.setQuantity(
                    productId = item.product.id,
                    selectedOption = item.selectedOption,
                    quantity = item.quantity - 1,
                )
            }.onFailure { _error.value = it.message }
        }
    }

    fun remove(item: CartItem) {
        viewModelScope.launch {
            runCatching {
                cartRepository.remove(
                    productId = item.product.id,
                    selectedOption = item.selectedOption,
                )
            }.onFailure { _error.value = it.message }
        }
    }

    fun requestClear() {
        _showClearDialog.value = true
    }

    fun dismissClear() {
        _showClearDialog.value = false
    }

    fun clear() {
        viewModelScope.launch {
            cartRepository.clear()
            _showClearDialog.value = false
        }
    }
}
