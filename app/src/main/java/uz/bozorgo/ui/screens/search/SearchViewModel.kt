package uz.bozorgo.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.core.util.TextNormalizer
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.model.ProductFilter
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.FavoriteRepository
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val results: List<Product> = emptyList(),
    val totalCount: Int = 0,
    val history: List<String> = emptyList(),
    val popularQueries: List<String> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
)

/**
 * Qidiruv: 1000+ mahsulot ichidan tez va to'g'ri natija.
 * Katta-kichik harf va apostrof farqi hisobga olinmaydi
 * (qarang: [TextNormalizer]).
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val preferences: UserPreferences,
    private val favoriteRepository: FavoriteRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val history = preferences.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val favoriteIds = favoriteRepository.observeIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val results = _query
        .debounce(250)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                kotlinx.coroutines.flow.flowOf(emptyList<Product>())
            } else {
                _isLoading.value = true
                productRepository.observeProducts(
                    filter = ProductFilter(query = query),
                    limit = SEARCH_LIMIT,
                    offset = 0,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val totalCount: StateFlow<Int> = results
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _isLoading,
        _error,
        results,
        totalCount,
    ) { query, isLoading, error, results, total ->
        SearchUiState(
            query = query,
            isLoading = isLoading,
            error = error,
            results = results,
            totalCount = total,
        )
    }
        .combine(history) { state, history -> state.copy(history = history) }
        .combine(favoriteIds) { state, ids -> state.copy(favoriteIds = ids, popularQueries = POPULAR_QUERIES) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState(popularQueries = POPULAR_QUERIES))

    init {
        viewModelScope.launch { productRepository.ensureSeeded() }
        // Natijalar kelganda yuklanish indikatorini yopish
        viewModelScope.launch {
            results.collect {
                _isLoading.value = false
                _error.value = null
            }
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
        if (query.trim().length >= 2) _isLoading.value = true
    }

    /** Qidiruv tarixiga yozish (natija ochilganda). */
    fun submit(query: String) {
        viewModelScope.launch {
            if (query.trim().length >= 2) preferences.addSearchQuery(query.trim())
        }
    }

    fun removeHistoryItem(query: String) {
        viewModelScope.launch { preferences.removeSearchQuery(query) }
    }

    fun clearHistory() {
        viewModelScope.launch { preferences.clearSearchHistory() }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch { runCatching { cartRepository.add(product) } }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch { runCatching { favoriteRepository.toggle(productId) } }
    }

    companion object {
        private const val SEARCH_LIMIT = 200
        val POPULAR_QUERIES = listOf("Sut", "Non", "Guruch", "Olma", "Shokolad", "Shampun", "Telefon")
    }
}
