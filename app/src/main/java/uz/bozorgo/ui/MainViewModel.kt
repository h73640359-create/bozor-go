package uz.bozorgo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.core.util.NetworkObserver
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Ilova darajasidagi holat: katalog tayyorligi, savatcha soni, internet holati.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    cartRepository: CartRepository,
    private val networkObserver: NetworkObserver,
    preferences: UserPreferences,
) : ViewModel() {

    private val _isPreparing = MutableStateFlow(true)
    val isPreparing: StateFlow<Boolean> = _isPreparing.asStateFlow()

    private val _catalogCount = MutableStateFlow(0)
    val catalogCount: StateFlow<Int> = _catalogCount.asStateFlow()

    private val _seedError = MutableStateFlow<String?>(null)
    val seedError: StateFlow<String?> = _seedError.asStateFlow()

    val cartCount: StateFlow<Int> = cartRepository.observeItemsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** "system" | "light" | "dark" */
    val themeMode: StateFlow<String> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val isOnline: StateFlow<Boolean> = networkObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), networkObserver.isCurrentlyConnected())

    init {
        prepareCatalog()
    }

    fun prepareCatalog() {
        viewModelScope.launch {
            _isPreparing.value = true
            runCatching { productRepository.ensureSeeded() }
                .onFailure { _seedError.value = it.message ?: "Katalogni yuklab bo'lmadi" }
            _catalogCount.value = runCatching { productRepository.countTotal() }.getOrDefault(0)
            _isPreparing.value = false
        }
    }

    /** Server sozlangan bo'lsa katalogni yangilash. */
    fun refreshFromServer() {
        viewModelScope.launch {
            productRepository.syncWithServer()
            _catalogCount.value = runCatching { productRepository.countTotal() }.getOrDefault(0)
        }
    }
}
