package uz.bozorgo.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.domain.repository.ProductRepository
import uz.bozorgo.domain.repository.UserRepository
import javax.inject.Inject

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val catalogCount: Int = 0,
    val showLogoutDialog: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    private val preferences: UserPreferences,
    productRepository: ProductRepository,
) : ViewModel() {

    private val _showLogoutDialog = MutableStateFlow(false)

    private val catalogCount = kotlinx.coroutines.flow.flow {
        emit(runCatching { productRepository.countTotal() }.getOrDefault(0))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.isLoggedIn,
        userRepository.currentUser,
        preferences.notificationsEnabled,
    ) { isLoggedIn, user, notifications ->
        ProfileUiState(
            isLoggedIn = isLoggedIn,
            name = user.name,
            phone = user.phone,
            isAdmin = user.isAdmin,
            notificationsEnabled = notifications,
        )
    }
        .combine(catalogCount) { state, count -> state.copy(catalogCount = count) }
        .combine(_showLogoutDialog) { state, show -> state.copy(showLogoutDialog = show) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun requestLogout() {
        _showLogoutDialog.value = true
    }

    fun dismissLogout() {
        _showLogoutDialog.value = false
    }

    fun logout() {
        viewModelScope.launch {
            preferences.clearSession()
            _showLogoutDialog.value = false
        }
    }
}
