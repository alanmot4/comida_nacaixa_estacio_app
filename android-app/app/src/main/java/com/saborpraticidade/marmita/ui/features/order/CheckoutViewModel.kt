package com.saborpraticidade.marmita.ui.features.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saborpraticidade.marmita.data.Order
import com.saborpraticidade.marmita.data.OrderItem
import com.saborpraticidade.marmita.data.repo.SupabaseRepository
import com.saborpraticidade.marmita.data.Payment
import com.saborpraticidade.marmita.data.cart.CartRepository
import com.saborpraticidade.marmita.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Em uma versão completa, os itens viriam do carrinho compartilhado via StateHolder/Repository

data class CheckoutUiState(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val phoneError: String? = null,
    val paymentMethod: String = "pix",
    val total: Double = 0.0,
    val items: List<OrderItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class CheckoutViewModel(
    private val repo: SupabaseRepository,
    private val cartRepo: CartRepository,
    private val profileRepo: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState

    init {
        // Observe cart changes to keep items and total in sync on the checkout screen
        viewModelScope.launch {
            cartRepo.items.collect { map ->
                val items = map.values.toList()
                val total = items.sumOf { it.price * it.quantity }
                _uiState.value = _uiState.value.copy(items = items, total = total)
            }
        }
        // Prefill user data from profile metadata
        viewModelScope.launch {
            runCatching {
                val basic = profileRepo.currentUserBasicInfo()
                val addr = profileRepo.currentUserAddress()
                val line1 = listOfNotNull(addr?.street?.takeIf { it.isNotBlank() }, addr?.number?.takeIf { it.isNotBlank() })
                    .joinToString(", ")
                val cityState = listOfNotNull(addr?.city?.takeIf { it.isNotBlank() }, addr?.state?.takeIf { it.isNotBlank() })
                    .joinToString(" - ")
                val parts = mutableListOf<String>()
                if (line1.isNotBlank()) parts.add(line1)
                if (!addr?.neighborhood.isNullOrBlank()) parts.add(addr?.neighborhood!!)
                if (cityState.isNotBlank()) parts.add(cityState)
                if (!addr?.cep.isNullOrBlank()) parts.add("CEP ${addr?.cep}")
                val addressText = parts.joinToString(" | ")

                // Prefill name
                if (!basic?.fullName.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(name = basic!!.fullName!!)
                }
                // Prefill phone using existing mask logic for consistency
                if (!basic?.phone.isNullOrBlank()) {
                    setPhoneMasked(basic!!.phone!!)
                }
                // Prefill address in one line
                if (addressText.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(address = addressText)
                }
            }
        }
    }

    fun setName(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun setPhone(v: String) { _uiState.value = _uiState.value.copy(phone = v) }
    fun setPhoneMasked(v: String) {
        val digits = v.filter { it.isDigit() }.take(11)
        val masked = if (digits.length <= 10) {
            when {
                digits.isEmpty() -> ""
                digits.length <= 2 -> "(${digits}"
                digits.length <= 6 -> "(${digits.substring(0,2)}) ${digits.substring(2)}"
                else -> "(${digits.substring(0,2)}) ${digits.substring(2,6)}-${digits.substring(6)}"
            }
        } else {
            when {
                digits.length <= 2 -> "(${digits}"
                digits.length <= 7 -> "(${digits.substring(0,2)}) ${digits.substring(2)}"
                else -> "(${digits.substring(0,2)}) ${digits.substring(2,7)}-${digits.substring(7)}"
            }
        }
        val error = if (digits.length in 10..11) null else "Telefone inválido"
        _uiState.value = _uiState.value.copy(phone = masked, phoneError = error)
    }
    fun setAddress(v: String) { _uiState.value = _uiState.value.copy(address = v) }
    fun setNotes(v: String) { _uiState.value = _uiState.value.copy(notes = v) }
    fun setPaymentMethod(v: String) { _uiState.value = _uiState.value.copy(paymentMethod = v) }

    fun submit(onDone: () -> Unit) {
        val s = _uiState.value
        if (s.name.isBlank() || s.phone.isBlank() || s.address.isBlank()) {
            _uiState.value = s.copy(error = "Preencha todos os campos obrigatórios")
            return
        }
        if (s.phoneError != null) {
            _uiState.value = s.copy(error = s.phoneError)
            return
        }
        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null)
            try {
                val items = cartRepo.items.value.values.toList()
                val total = cartRepo.total()
                val order = Order(
                    customer_name = s.name,
                    customer_phone = s.phone.filter { it.isDigit() },
                    customer_address = s.address,
                    items = if (s.items.isNotEmpty()) s.items else items,
                    total = if (s.total > 0.0) s.total else total,
                    notes = if (s.notes.isBlank()) null else s.notes,
                )
                val created = repo.createOrder(order)
                // Create payment record (mock) when we have an order id
                created.id?.let { oid ->
                    repo.createPayment(
                        Payment(
                            order_id = oid,
                            method = s.paymentMethod,
                            amount = created.total,
                        )
                    )
                }
                cartRepo.clear()
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Erro ao criar pedido")
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}
