
package com.saborpraticidade.marmita.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saborpraticidade.marmita.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, saved = false)
            try {
                val p = repo.getMyProfile()
                val roles = repo.getMyRoles()
                val email = repo.currentUserEmail()
                val addr = repo.currentUserAddress()
                val basic = repo.currentUserBasicInfo()
                val line1 = listOfNotNull(addr?.street?.takeIf { it.isNotBlank() }, addr?.number?.takeIf { it.isNotBlank() })
                    .joinToString(separator = ", ")
                    .ifBlank { null }
                val line2 = buildList {
                    if (!addr?.neighborhood.isNullOrBlank()) add(addr?.neighborhood!!)
                    val cityState = listOfNotNull(addr?.city?.takeIf { it.isNotBlank() }, addr?.state?.takeIf { it.isNotBlank() })
                        .joinToString(separator = " - ")
                    if (cityState.isNotBlank()) add(cityState)
                    if (!addr?.cep.isNullOrBlank()) add("CEP ${addr?.cep}")
                }.joinToString(separator = " | ").ifBlank { null }
                val resolvedName = (p?.full_name)?.takeIf { !it.isNullOrBlank() } ?: (basic?.fullName ?: "")
                val resolvedPhone = (p?.phone)?.takeIf { !it.isNullOrBlank() } ?: (basic?.phone ?: "")

                _uiState.value = _uiState.value.copy(
                    fullName = resolvedName,
                    phone = resolvedPhone,
                    email = email,
                    addressLine1 = line1,
                    addressLine2 = line2,
                    roles = roles,
                    loading = false
                )

                // Opcional: se ainda não existe em profiles, grava os valores do metadata
                if ((p?.full_name.isNullOrBlank() && !resolvedName.isNullOrBlank()) || (p?.phone.isNullOrBlank() && !resolvedPhone.isNullOrBlank())) {
                    runCatching { repo.upsertMyProfile(fullName = resolvedName.ifBlank { null }, phone = resolvedPhone.ifBlank { null }) }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.message ?: "Erro ao carregar perfil")
            }
        }
    }

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v) }

    fun save() {
        val s = _uiState.value
        // Simple validation
        if (s.phone.isNotBlank() && s.phone.length < 10) {
            _uiState.value = s.copy(error = "Informe um telefone válido")
            return
        }
        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null, saved = false)
            try {
                repo.upsertMyProfile(fullName = s.fullName.ifBlank { null }, phone = s.phone.ifBlank { null })
                // refresh roles and profile after save
                val roles = runCatching { repo.getMyRoles() }.getOrDefault(_uiState.value.roles)
                _uiState.value = _uiState.value.copy(loading = false, saved = true, roles = roles)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.message ?: "Erro ao salvar perfil")
            }
        }
    }

    fun ackSaved() {
        _uiState.value = _uiState.value.copy(saved = false)
    }
}