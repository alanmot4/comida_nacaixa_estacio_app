package com.saborpraticidade.marmita.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saborpraticidade.marmita.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository,
    private val viaCep: com.saborpraticidade.marmita.data.geo.ViaCepService
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v) }
    fun onCepChange(v: String) {
        _uiState.value = _uiState.value.copy(cep = v)
        val digits = v.filter { it.isDigit() }
        if (digits.length == 8) {
            // Trigger ViaCEP lookup
            viewModelScope.launch {
                val res = runCatching { viaCep.lookup(digits) }.getOrNull()
                if (res != null) {
                    _uiState.value = _uiState.value.copy(
                        street = res.logradouro ?: _uiState.value.street,
                        neighborhood = res.bairro ?: _uiState.value.neighborhood,
                        city = res.localidade ?: _uiState.value.city,
                        stateUf = res.uf ?: _uiState.value.stateUf
                    )
                }
            }
        }
    }
    fun onStreetChange(v: String) { _uiState.value = _uiState.value.copy(street = v) }
    fun onNumberChange(v: String) { _uiState.value = _uiState.value.copy(number = v) }
    fun onComplementChange(v: String) { _uiState.value = _uiState.value.copy(complement = v) }
    fun onNeighborhoodChange(v: String) { _uiState.value = _uiState.value.copy(neighborhood = v) }
    fun onCityChange(v: String) { _uiState.value = _uiState.value.copy(city = v) }
    fun onStateUfChange(v: String) { _uiState.value = _uiState.value.copy(stateUf = v) }

    fun signIn(onSuccess: () -> Unit) = doAuth(onSuccess) {
        repo.signIn(_uiState.value.email.trim(), _uiState.value.password)
    }

    fun signUp(onSuccess: () -> Unit) {
        val s = _uiState.value
        val email = s.email.trim()
        val pass = s.password
        val name = s.fullName.trim()
        val phone = s.phone.trim()

        // Simple validation for required fields
        if (name.isBlank()) {
            _uiState.value = s.copy(error = "Informe seu nome completo.")
            return
        }
        if (phone.isBlank() || phone.filter { it.isDigit() }.length < 8) {
            _uiState.value = s.copy(error = "Informe um telefone válido.")
            return
        }
        // Validate address required fields for sign-up
        val cepDigits = s.cep.filter { it.isDigit() }
        if (cepDigits.length != 8) {
            _uiState.value = s.copy(error = "Informe um CEP válido com 8 dígitos.")
            return
        }
        if (s.street.isBlank()) { _uiState.value = s.copy(error = "Informe o logradouro."); return }
        if (s.number.isBlank()) { _uiState.value = s.copy(error = "Informe o número."); return }
        if (s.neighborhood.isBlank()) { _uiState.value = s.copy(error = "Informe o bairro."); return }
        if (s.city.isBlank()) { _uiState.value = s.copy(error = "Informe a cidade."); return }
        if (s.stateUf.isBlank()) { _uiState.value = s.copy(error = "Informe a UF."); return }

        doAuth(onSuccess) {
            repo.signUp(
                email,
                pass,
                name,
                phone,
                mapOf(
                    "cep" to cepDigits,
                    "street" to s.street,
                    "number" to s.number,
                    "complement" to s.complement,
                    "neighborhood" to s.neighborhood,
                    "city" to s.city,
                    "state" to s.stateUf
                )
            )
        }
    }

    private fun doAuth(onSuccess: () -> Unit, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                block()
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Erro ao autenticar"
                _uiState.value = _uiState.value.copy(error = msg)
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}