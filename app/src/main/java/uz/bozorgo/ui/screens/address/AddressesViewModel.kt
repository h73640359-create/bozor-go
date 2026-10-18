package uz.bozorgo.ui.screens.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.domain.model.Address
import uz.bozorgo.domain.repository.AddressRepository
import javax.inject.Inject

data class AddressesUiState(
    val addresses: List<Address> = emptyList(),
)

@HiltViewModel
class AddressesViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
) : ViewModel() {

    val uiState: StateFlow<AddressesUiState> = addressRepository.observeAddresses()
        .map { AddressesUiState(addresses = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddressesUiState())

    fun save(address: Address) {
        viewModelScope.launch { runCatching { addressRepository.save(address) } }
    }

    fun delete(id: String) {
        viewModelScope.launch { runCatching { addressRepository.delete(id) } }
    }

    fun makeDefault(id: String) {
        viewModelScope.launch {
            val address = addressRepository.getById(id) ?: return@launch
            addressRepository.save(address.copy(isDefault = true))
        }
    }
}
