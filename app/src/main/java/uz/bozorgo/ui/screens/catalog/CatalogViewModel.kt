package uz.bozorgo.ui.screens.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.core.common.Constants
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.model.ProductFilter
import uz.bozorgo.domain.model.SortOption
import uz.bozorgo.domain.model.Subcategory
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.CatalogRepository
import uz.bozorgo.domain.repository.FavoriteRepository
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

data class CatalogUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val totalCount: Int = 0,
    val subcategories: List<Subcategory> = emptyList(),
    val category: Category? = null,
    val filter: ProductFilter = ProductFilter(),
    val priceRange: ClosedFloatingPointRange<Double> = 0.0..1_000_000.0,
    val brands: List<String> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
)

/**
 * Katalog, kategoriya, to'plam ("Barchasini ko'rish") va qidiruv natijalari
 * uchun yagona ViewModel. Filtr, saralash va sahifalab yuklash shu yerda.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val catalogRepository: CatalogRepository,
    private val favoriteRepository: FavoriteRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val categoryId: String? = savedStateHandle.get<String>("categoryId")
    private val subcategoryId: String? = savedStateHandle.get<String>("subcategoryId")
    private val collectionType: String? = savedStateHandle.get<String>("type")
    private val initialQuery: String = savedStateHandle.get<String>("query").orEmpty()

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _totalCount = MutableStateFlow(0)
    private val _category = MutableStateFlow<Category?>(null)
    private val _priceRange =
        MutableStateFlow<ClosedFloatingPointRange<Double>>(0.0..1_000_000.0)
    private val _brands = MutableStateFlow<List<String>>(emptyList())
    private val _visibleCount = MutableStateFlow(Constants.PAGE_SIZE)

    private val _filter = MutableStateFlow(
        buildFilter(
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            query = initialQuery,
            collection = collectionType,
        ),
    )

    private val _subcategories = MutableStateFlow<List<Subcategory>>(emptyList())

    private val favoriteIds = favoriteRepository.observeIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Filtr o'zgarganda mahsulotlar qayta yuklanadi (sahifalash bilan). */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val products: StateFlow<List<Product>> = _filter
        .debounce(200)
        .distinctUntilChanged()
        .combine(_visibleCount) { filter, count -> filter to count }
        .flatMapLatest { (filter, count) ->
            productRepository.observeProducts(filter = filter, limit = count, offset = 0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<CatalogUiState> = combine(
        _isLoading,
        _error,
        products,
        _totalCount,
        _subcategories,
    ) { isLoading, error, products, totalCount, subcategories ->
        CatalogUiState(
            isLoading = isLoading,
            error = error,
            products = products,
            totalCount = totalCount,
            subcategories = subcategories,
        )
    }
        .combine(_category) { state, category -> state.copy(category = category) }
        .combine(_filter) { state, filter -> state.copy(filter = filter) }
        .combine(_priceRange) { state, range -> state.copy(priceRange = range) }
        .combine(_brands) { state, brands -> state.copy(brands = brands) }
        .combine(favoriteIds) { state, ids -> state.copy(favoriteIds = ids) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    init {
        loadMeta()
        observeCounts()
    }

    private fun loadMeta() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                productRepository.ensureSeeded()
                _priceRange.value = productRepository.priceRange().let { it.first..it.second }
                _brands.value = productRepository.allBrands().take(40)
                val category = categoryId?.let { catalogRepository.getCategory(it) }
                _category.value = category
                _subcategories.value = category?.subcategories.orEmpty()
            }.onFailure {
                _error.value = it.message
            }
            _isLoading.value = false
        }
    }

    /** Filtr o'zgarganda umumiy sonni qayta hisoblash. */
    private fun observeCounts() {
        viewModelScope.launch {
            _filter
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { filter ->
                    _totalCount.value = runCatching { productRepository.countProducts(filter) }
                        .getOrDefault(0)
                    _visibleCount.value = Constants.PAGE_SIZE
                }
        }
    }

    fun selectSubcategory(subcategoryId: String?) {
        _filter.value = _filter.value.copy(subcategoryId = subcategoryId)
    }

    fun setSort(sort: SortOption) {
        _filter.value = _filter.value.copy(sort = sort)
    }

    fun updateFilter(onlyDiscount: Boolean, inStockOnly: Boolean) {
        _filter.value = _filter.value.copy(
            onlyDiscount = onlyDiscount,
            inStockOnly = inStockOnly,
        )
    }

    fun setPriceRange(min: Double, max: Double) {
        val range = _priceRange.value
        val isFullRange = min <= range.start && max >= range.endInclusive
        _filter.value = _filter.value.copy(
            minPrice = if (isFullRange) null else min,
            maxPrice = if (isFullRange) null else max,
        )
    }

    fun toggleBrand(brand: String) {
        val current = _filter.value.brands.toMutableList()
        if (current.contains(brand)) current.remove(brand) else current.add(brand)
        _filter.value = _filter.value.copy(brands = current)
    }

    fun resetFilters() {
        _filter.value = buildFilter(
            categoryId = categoryId,
            subcategoryId = null,
            query = initialQuery,
            collection = collectionType,
        )
    }

    fun loadMore() {
        if (_visibleCount.value >= _totalCount.value) return
        _visibleCount.value = _visibleCount.value + Constants.PAGE_SIZE
    }

    fun refresh() {
        loadMeta()
    }

    fun addToCart(product: Product) {
        viewModelScope.launch { runCatching { cartRepository.add(product) } }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch { runCatching { favoriteRepository.toggle(productId) } }
    }

    companion object {
        fun buildFilter(
            categoryId: String?,
            subcategoryId: String?,
            query: String,
            collection: String?,
        ): ProductFilter = when (collection) {
            "deals" -> ProductFilter(onlyDiscount = true, sort = SortOption.DISCOUNT)
            "new" -> ProductFilter(sort = SortOption.NEWEST)
            "recommended" -> ProductFilter(sort = SortOption.POPULAR)
            "popular" -> ProductFilter(sort = SortOption.POPULAR)
            else -> ProductFilter(
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                query = query,
            )
        }
    }
}
