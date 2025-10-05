package com.saborpraticidade.marmita.ui.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saborpraticidade.marmita.data.Order
import com.saborpraticidade.marmita.data.repo.SupabaseRepository
import com.saborpraticidade.marmita.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MyOrdersUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val orders: List<Order> = emptyList()
)

class MyOrdersViewModel(
    private val repo: SupabaseRepository,
    private val profileRepo: ProfileRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(MyOrdersUiState(loading = true))
    val ui: StateFlow<MyOrdersUiState> = _ui

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val basic = runCatching { profileRepo.currentUserBasicInfo() }.getOrNull()
                val phoneDigits = basic?.phone?.filter { it.isDigit() }
                if (phoneDigits.isNullOrBlank()) {
                    _ui.value = MyOrdersUiState(loading = false, error = "Informe seu telefone no perfil para localizar seus pedidos")
                    return@launch
                }
                val list = repo.listMyOrders(phoneDigits)
                _ui.value = _ui.value.copy(loading = false, orders = list)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Falha ao carregar pedidos")
            }
        }
    }
}
