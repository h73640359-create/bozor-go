package uz.bozorgo.ui.screens.checkout

import uz.bozorgo.core.util.isValidUzPhone
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.core.common.Resource
import uz.bozorgo.core.util.NetworkObserver
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.data.remote.ApiConfig
import uz.bozorgo.data.remote.ApiService
import uz.bozorgo.data.remote.dto.PaymentRequestDto
import uz.bozorgo.domain.model.Address
import uz.bozorgo.domain.model.CartSummary
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.model.PaymentMethod
import uz.bozorgo.domain.repository.AddressRepository
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.CreateOrderParams
import uz.bozorgo.domain.repository.OrderRepository
import uz.bozorgo.domain.usecase.CartCalculator
import javax.inject.Inject

data class CheckoutUiState(
    val items: List<uz.bozorgo.domain.model.CartItem> = emptyList(),
    val summary: CartSummary = CartSummary.empty(),
    val deliveryFees: Map<DeliveryMethod, Double> = emptyMap(),
    val name: String = "",
    val phone: String = "",
    val comment: String = "",
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String = "",
    val deliveryMethod: DeliveryMethod = DeliveryMethod.STANDARD,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isPlacing: Boolean = false,
    val error: String? = null,
    val paymentMessage: String? = null,
    val isServerConfigured: Boolean = false,
    val isOnline: Boolean = true,
    val nameError: Int? = null,
    val phoneError: Int? = null,
    val addressError: Int? = null,
) {
    fun deliveryFeeFor(method: DeliveryMethod): Double = deliveryFees[method] ?: 0.0

    val selectedAddress: Address? get() = addresses.firstOrNull { it.id == selectedAddressId }
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository,
    private val preferences: UserPreferences,
    private val calculator: CartCalculator,
    private val apiService: ApiService,
    private val apiConfig: ApiConfig,
    networkObserver: NetworkObserver,
) : ViewModel() {

    private val _name = MutableStateFlow("")
    private val _phone = MutableStateFlow("")
    private val _comment = MutableStateFlow("")
    private val _selectedAddressId = MutableStateFlow("")
    private val _deliveryMethod = MutableStateFlow(DeliveryMethod.STANDARD)
    private val _paymentMethod = MutableStateFlow(PaymentMethod.CASH)
    private val _isPlacing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _paymentMessage = MutableStateFlow<String?>(null)
    private val _nameError = MutableStateFlow<Int?>(null)
    private val _phoneError = MutableStateFlow<Int?>(null)
    private val _addressError = MutableStateFlow<Int?>(null)

    private val items = cartRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val addresses = addressRepository.observeAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val summary: StateFlow<CartSummary> = combine(items, _deliveryMethod) { items, method ->
        calculator.calculate(items, method)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartSummary.empty())

    val uiState: StateFlow<CheckoutUiState> = combine(items, summary, addresses) { items, summary, addresses ->
        CheckoutUiState(
            items = items,
            summary = summary,
            addresses = addresses,
            isServerConfigured = apiConfig.isConfigured,
        )
    }
        .combine(_name) { state, name -> state.copy(name = name) }
        .combine(_phone) { state, phone -> state.copy(phone = phone) }
        .combine(_comment) { state, comment -> state.copy(comment = comment) }
        .combine(_selectedAddressId) { state, id -> state.copy(selectedAddressId = id) }
        .combine(_deliveryMethod) { state, method ->
            state.copy(
                deliveryMethod = method,
                deliveryFees = DeliveryMethod.entries.associateWith { delivery ->
                    calculator.deliveryFeeFor(state.summary.itemsTotal, delivery)
                },
            )
        }
        .combine(_paymentMethod) { state, method -> state.copy(paymentMethod = method) }
        .combine(_isPlacing) { state, placing -> state.copy(isPlacing = placing) }
        .combine(_error) { state, error -> state.copy(error = error) }
        .combine(_paymentMessage) { state, message -> state.copy(paymentMessage = message) }
        .combine(_nameError) { state, error -> state.copy(nameError = error) }
        .combine(_phoneError) { state, error -> state.copy(phoneError = error) }
        .combine(_addressError) { state, error -> state.copy(addressError = error) }
        .combine(networkObserver.isOnline) { state, online -> state.copy(isOnline = online) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CheckoutUiState(isServerConfigured = apiConfig.isConfigured),
        )

    init {
        viewModelScope.launch {
            _name.value = preferences.userName.first()
            _phone.value = preferences.userPhone.first()
            val defaultAddress = runCatching { addressRepository.observeDefault().first() }.getOrNull()
            _selectedAddressId.value = defaultAddress?.id.orEmpty()
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
        _nameError.value = null
    }

    fun onPhoneChange(value: String) {
        _phone.value = value
        _phoneError.value = null
    }

    fun onCommentChange(value: String) {
        _comment.value = value
    }

    fun onAddressSelect(addressId: String) {
        _selectedAddressId.value = addressId
        _addressError.value = null
    }

    fun onDeliveryMethodChange(method: DeliveryMethod) {
        _deliveryMethod.value = method
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _paymentMethod.value = method
        if (method == PaymentMethod.CARD && !apiConfig.isConfigured) {
            _paymentMessage.value = null
        }
    }

    /**
     * Buyurtmani rasmiylashtirish.
     * Server sozlangan bo'lsa — faqat server javobi muvaffaqiyatli bo'lganda
     * "qabul qilindi" deb hisoblanadi.
     */
    fun placeOrder(onSuccess: (String) -> Unit) {
        if (_isPlacing.value) return // takroriy yuborishdan himoya
        viewModelScope.launch {
            val currentItems = items.value
            val address = addresses.value.firstOrNull { it.id == _selectedAddressId.value }
                ?: addresses.value.firstOrNull { it.isDefault }

            val nameValid = _name.value.trim().length >= 2
            val phoneValid = isValidUzPhone(_phone.value)
            _nameError.value = if (nameValid) null else R.string.checkout_error_name
            _phoneError.value = if (phoneValid) null else R.string.checkout_error_phone
            _addressError.value = if (address != null) null else R.string.checkout_error_address
            if (!nameValid || !phoneValid || address == null) return@launch

            val currentSummary = calculator.calculate(currentItems, _deliveryMethod.value)
            if (currentItems.isEmpty()) return@launch

            _isPlacing.value = true
            _error.value = null
            _paymentMessage.value = null

            val result: Resource<Order> = orderRepository.createOrder(
                CreateOrderParams(
                    items = currentItems,
                    summary = currentSummary,
                    deliveryMethod = _deliveryMethod.value,
                    paymentMethod = _paymentMethod.value,
                    recipientName = _name.value.trim(),
                    phone = _phone.value.trim(),
                    addressTitle = address.title,
                    addressText = address.fullAddress,
                    comment = _comment.value.trim(),
                ),
            )

            when (result) {
                is Resource.Success -> {
                    val order = result.data
                    preferences.updateProfile(_name.value.trim(), _phone.value.trim())
                    if (_paymentMethod.value == PaymentMethod.CARD) {
                        _paymentMessage.value = processCardPayment(order.id)
                    }
                    cartRepository.clear()
                    _isPlacing.value = false
                    onSuccess(order.id)
                }

                is Resource.Error -> {
                    _error.value = result.message
                    _isPlacing.value = false
                }

                is Resource.Loading -> Unit
            }
        }
    }

    /**
     * Karta orqali to'lov.
     * HAQIQIY provayder ulanmagani uchun bu demo: hech qanday pul o'tkazilmaydi.
     */
    private suspend fun processCardPayment(orderId: String): String {
        if (!apiConfig.isConfigured) {
            return "To'lov provayderi ulanmagan: karta orqali to'lov demo rejimda, haqiqiy pul o'tkazilmadi."
        }
        return try {
            val response = apiService.createPayment(
                PaymentRequestDto(orderId = orderId, method = PaymentMethod.CARD.key),
            )
            response.message ?: "To'lov holati: ${response.status}"
        } catch (t: Throwable) {
            "To'lovni boshlab bo'lmadi: ${t.message}"
        }
    }
}
