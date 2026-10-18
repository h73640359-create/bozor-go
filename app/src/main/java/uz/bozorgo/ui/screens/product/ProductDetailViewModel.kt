package uz.bozorgo.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.FavoriteRepository
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val product: Product? = null,
    val similar: List<Product> = emptyList(),
    val similarFavoriteIds: Set<String> = emptySet(),
    val isFavorite: Boolean = false,
    val quantity: Int = 1,
    val selectedOption: String? = null,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val productId: String = savedStateHandle.get<String>("productId") ?: ""

    private val _quantity = MutableStateFlow(1)
    private val _selectedOption = MutableStateFlow<String?>(null)
    private val _similar = MutableStateFlow<List<Product>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)

    private val productFlow: StateFlow<Product?> =
        flow { emit(productRepository.getProduct(productId)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val isFavorite: StateFlow<Boolean> =
        favoriteRepository.observeIsFavorite(productId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val favoriteIds: StateFlow<Set<String>> = favoriteRepository.observeIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val uiState: StateFlow<ProductDetailUiState> = combine(
        productFlow,
        isFavorite,
        _quantity,
        _selectedOption,
        _similar,
    ) { product, favorite, quantity, option, similar ->
        ProductDetailUiState(
            isLoading = product == null,
            product = product,
            isFavorite = favorite,
            quantity = quantity,
            selectedOption = option,
            similar = similar,
        )
    }
        .combine(favoriteIds) { state, ids -> state.copy(similarFavoriteIds = ids) }
        .combine(_error) { state, error -> state.copy(error = error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductDetailUiState())

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _error.value = null
            runCatching {
                productRepository.ensureSeeded()
                productRepository.getSimilar(productId)
            }.onSuccess { similar ->
                _similar.value = similar
            }.onFailure { throwable ->
                _error.value = throwable.message
            }
        }
    }

    fun selectOption(option: String) {
        _selectedOption.value = option
    }

    fun setQuantity(quantity: Int) {
        _quantity.value = quantity.coerceIn(1, 99)
    }

    /** Joriy mahsulotni savatchaga qo'shish. */
    fun addToCart(onSuccess: () -> Unit, onOutOfStock: () -> Unit) {
        viewModelScope.launch {
            val product = productFlow.value
            if (product == null || !product.inStock || product.stock <= 0) {
                onOutOfStock()
                return@launch
            }
            val result = runCatching {
                cartRepository.add(
                    product = product,
                    quantity = _quantity.value.coerceAtMost(product.stock),
                    selectedOption = _selectedOption.value,
                )
            }
            if (result.isSuccess) onSuccess() else onOutOfStock()
        }
    }

    fun addToCartDirect(product: Product) {
        viewModelScope.launch { runCatching { cartRepository.add(product) } }
    }

    /** Joriy mahsulotni sevimlilarga qo'shish / olib tashlash. */
    fun toggleCurrentFavorite() {
        viewModelScope.launch { runCatching { favoriteRepository.toggle(productId) } }
    }

    /** "O'xshash mahsulotlar" ro'yxatidagi mahsulot uchun. */
    fun toggleFavorite(productId: String) {
        viewModelScope.launch { runCatching { favoriteRepository.toggle(productId) } }
    }

    fun retry() {
        loadProduct()
    }
}
