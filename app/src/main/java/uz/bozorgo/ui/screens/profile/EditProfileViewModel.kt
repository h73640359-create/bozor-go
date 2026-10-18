package uz.bozorgo.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.bozorgo.data.local.datastore.UserPreferences
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val error: String? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val preferences: UserPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = preferences.currentUser()
            _state.value = EditProfileUiState(name = user.name, phone = user.phone)
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value, error = null)
    }

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value, error = null)
    }

    fun save() {
        val state = _state.value
        val name = state.name.trim()
        val digits = state.phone.filter { it.isDigit() }
        if (name.length < 2) {
            _state.value = state.copy(error = "Ism kamida 2 belgi bo'lishi kerak")
            return
        }
        if (digits.length != 12 || !digits.startsWith("998")) {
            _state.value = state.copy(error = "Telefon +998 XX XXX XX XX shaklida bo'lishi kerak")
            return
        }
        viewModelScope.launch {
            preferences.updateProfile(name, state.phone.trim())
            _state.value = _state.value.copy(isSaved = true, error = null)
        }
    }
}
