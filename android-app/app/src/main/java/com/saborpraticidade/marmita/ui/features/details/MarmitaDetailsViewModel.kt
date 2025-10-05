package com.saborpraticidade.marmita.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saborpraticidade.marmita.data.Marmita
import com.saborpraticidade.marmita.data.repo.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MarmitaDetailsState(
    val loading: Boolean = false,
    val item: Marmita? = null,
    val error: String? = null,
)

class MarmitaDetailsViewModel(private val repo: SupabaseRepository): ViewModel() {
    private val _ui = MutableStateFlow(MarmitaDetailsState())
    val ui: StateFlow<MarmitaDetailsState> = _ui

    fun load(id: String) {
        viewModelScope.launch {
            _ui.value = MarmitaDetailsState(loading = true)
            try {
                val m = repo.getMarmita(id)
                _ui.value = MarmitaDetailsState(item = m, loading = false)
            } catch (e: Exception) {
                _ui.value = MarmitaDetailsState(error = e.message ?: "Erro ao carregar produto", loading = false)
            }
        }
    }
}
