package uz.bozorgo.ui.screens.home

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
import uz.bozorgo.domain.model.Banner
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.repository.AddressRepository
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.CatalogRepository
import uz.bozorgo.domain.repository.FavoriteRepository
import uz.bozorgo.domain.repository.HomeSections
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val banners: List<Banner> = emptyList(),
    val categories: List<Category> = emptyList(),
    val sections: HomeSections? = null,
    val favoriteIds: Set<String> = emptySet(),
    val addressText: String = "",
    val cartCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
    private val favoriteRepository: FavoriteRepository,
    addressRepository: AddressRepository,
) : ViewModel() {

    private val _sections = MutableStateFlow<HomeSections?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    private val _favoriteIds = favoriteRepository.observeIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _cartCount = cartRepository.observeItemsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _address = addressRepository.observeDefault()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<HomeUiState> = combine(
        _isLoading,
        _error,
        _banners,
        _categories,
        _sections,
    ) { isLoading, error, banners, categories, sections ->
        HomeUiState(
            isLoading = isLoading,
            error = error,
            banners = banners,
            categories = categories,
            sections = sections,
        )
    }
        .combine(_favoriteIds) { state, ids -> state.copy(favoriteIds = ids) }
        .combine(_cartCount) { state, count -> state.copy(cartCount = count) }
        .combine(_address) { state, address -> state.copy(addressText = address?.fullAddress.orEmpty()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching {
                productRepository.ensureSeeded()
                _categories.value = catalogRepository.getCategories()
                _banners.value = catalogRepository.getBanners()
                productRepository.getHomeSections()
            }.onSuccess { sections ->
                _sections.value = sections
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "Ma'lumotlarni yuklab bo'lmadi"
            }
            _isLoading.value = false
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            runCatching { cartRepository.add(product) }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            runCatching { favoriteRepository.toggle(productId) }
        }
    }
}
