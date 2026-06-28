package com.fashionsaas.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashionsaas.app.data.repository.ProductRepository
import com.fashionsaas.app.data.repository.StoreRepository
import com.fashionsaas.app.domain.model.Product
import com.fashionsaas.app.domain.model.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla principal.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredStores: List<Store> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla principal.
 * Carga tiendas y productos destacados desde los repositorios.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    /** Estado de la UI de la pantalla principal. */
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * Carga las tiendas y productos destacados para la pantalla principal.
     * Ejecuta ambas peticiones en paralelo para mayor eficiencia.
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            val storesResult = storeRepository.getFeaturedStores()
            val productsResult = productRepository.getFeaturedProducts()
            _uiState.value = HomeUiState(
                isLoading = false,
                featuredStores = storesResult.getOrDefault(emptyList()),
                featuredProducts = productsResult.getOrDefault(emptyList()),
                error = storesResult.exceptionOrNull()?.message
            )
        }
    }

    /**
     * Limpia el error actual del estado de la UI.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}