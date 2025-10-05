package com.saborpraticidade.marmita.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saborpraticidade.marmita.data.repo.SupabaseRepository
import com.saborpraticidade.marmita.data.cart.CartRepository
import com.saborpraticidade.marmita.data.repo.SettingsRepository
import com.saborpraticidade.marmita.data.repo.ProfileRepository
import com.saborpraticidade.marmita.data.Marmita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val marmitas: Flow<androidx.paging.PagingData<Marmita>>? = null,
    val cartCount: Int = 0,
    val bannerUrl: String? = null,
    val isAdmin: Boolean = false,
    val logoUrl: String? = null,
    val storeName: String = "Sabor & Praticidade",
    val logoSizeDp: Int = 24,
)

class HomeViewModel(
    private val repo: SupabaseRepository,
    private val cartRepo: CartRepository,
    private val settings: SettingsRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val cart = mutableMapOf<String, Int>()

    init {
        loadMarmitas()
        loadBanner()
        loadLogo()
        loadStoreName()
        loadLogoSize()
        loadRoles()
    }

    fun loadMarmitas() {
        val pagingFlow = Pager(PagingConfig(pageSize = 12, prefetchDistance = 6)) {
            object : PagingSource<Int, Marmita>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Marmita> {
                    val offset = params.key ?: 0
                    return try {
                        val data = repo.listMarmitasPage(limit = params.loadSize, offset = offset).filter { it.available != false }
                        val nextKey = if (data.isEmpty()) null else offset + data.size
                        val prevKey = if (offset == 0) null else (offset - params.loadSize).coerceAtLeast(0)
                        LoadResult.Page(data = data, prevKey = prevKey, nextKey = nextKey)
                    } catch (e: Exception) {
                        LoadResult.Error(e)
                    }
                }
                override fun getRefreshKey(state: PagingState<Int, Marmita>): Int? {
                    val anchor = state.anchorPosition ?: return null
                    val page = state.closestPageToPosition(anchor)
                    return page?.prevKey?.plus(state.config.pageSize) ?: page?.nextKey?.minus(state.config.pageSize)
                }
            }
        }.flow.cachedIn(viewModelScope)

        _uiState.value = _uiState.value.copy(loading = false, marmitas = pagingFlow)
    }

    fun addToCart(item: Marmita) {
        cartRepo.add(item)
        _uiState.value = _uiState.value.copy(cartCount = cartRepo.items.value.values.sumOf { it.quantity })
    }

    fun loadBanner() {
        viewModelScope.launch {
            runCatching { settings.getBannerUrl() }
                .onSuccess { url -> _uiState.value = _uiState.value.copy(bannerUrl = url) }
        }
    }

    fun refreshBanner() = loadBanner()

    private fun loadLogo() {
        viewModelScope.launch {
            runCatching { settings.getLogoUrl() }
                .onSuccess { url -> _uiState.value = _uiState.value.copy(logoUrl = url) }
        }
    }

    fun refreshLogo() = loadLogo()

    private fun loadStoreName() {
        viewModelScope.launch {
            runCatching { settings.getStoreName() }
                .onSuccess { name -> if (!name.isNullOrBlank()) _uiState.value = _uiState.value.copy(storeName = name) }
        }
    }

    fun refreshStoreName() = loadStoreName()

    private fun loadLogoSize() {
        viewModelScope.launch {
            runCatching { settings.getLogoSizeDp() }
                .onSuccess { s -> if (s != null && s in 16..96) _uiState.value = _uiState.value.copy(logoSizeDp = s) }
        }
    }

    fun refreshLogoSize() = loadLogoSize()

    private fun loadRoles() {
        viewModelScope.launch {
            runCatching { profileRepo.getMyRoles() }
                .onSuccess { roles -> _uiState.value = _uiState.value.copy(isAdmin = roles.any { it.equals("admin", ignoreCase = true) }) }
                .onFailure { /* ignore when logged out */ }
        }
    }
}
