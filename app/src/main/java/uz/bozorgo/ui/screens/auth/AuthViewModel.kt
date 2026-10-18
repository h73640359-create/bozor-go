package uz.bozorgo.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.core.common.Resource
import uz.bozorgo.domain.repository.UserRepository
import javax.inject.Inject

enum class AuthStep { PHONE, CODE }

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE,
    val phone: String = "+998 ",
    val code: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val demoCode: String = "",
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value, error = null)
    }

    fun onCodeChange(value: String) {
        _state.value = _state.value.copy(code = value.filter { it.isDigit() }.take(6), error = null)
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun resetToPhoneStep() {
        _state.value = _state.value.copy(step = AuthStep.PHONE, code = "", demoCode = "", error = null)
    }

    fun requestCode() {
        viewModelScope.launch {
            val phone = _state.value.phone.trim()
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = userRepository.requestOtp(phone)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        step = AuthStep.CODE,
                        demoCode = result.data,
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    fun verify() {
        viewModelScope.launch {
            val state = _state.value
            if (state.code.length != 6) {
                _state.value = state.copy(error = "Kod 6 xonali bo'lishi kerak")
                return@launch
            }
            _state.value = state.copy(isLoading = true, error = null)
            when (val result = userRepository.verifyOtp(state.phone.trim(), state.code, state.name)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isAuthenticated = true)
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }
}
