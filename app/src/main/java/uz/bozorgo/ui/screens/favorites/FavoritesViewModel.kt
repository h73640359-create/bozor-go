package uz.bozorgo.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.FavoriteRepository
import javax.inject.Inject

data class FavoritesUiState(
    val products: List<Product> = emptyList(),
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoriteRepository.observeFavorites()
        .map { FavoritesUiState(products = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    fun remove(productId: String) {
        viewModelScope.launch { favoriteRepository.remove(productId) }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch { runCatching { cartRepository.add(product) } }
    }
}
