package uz.bozorgo.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.repository.CatalogRepository
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

data class CatalogRootUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val totalProducts: Int = 0,
)

@HiltViewModel
class CatalogRootViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogRootUiState())
    val uiState: StateFlow<CatalogRootUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                productRepository.ensureSeeded()
                val categories = catalogRepository.getCategories()
                val total = productRepository.countTotal()
                categories to total
            }.onSuccess { (categories, total) ->
                _state.value = CatalogRootUiState(
                    isLoading = false,
                    categories = categories,
                    totalProducts = total,
                )
            }.onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Katalogni yuklab bo'lmadi",
                )
            }
        }
    }
}
