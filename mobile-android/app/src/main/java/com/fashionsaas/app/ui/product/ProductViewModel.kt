package com.fashionsaas.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashionsaas.app.data.repository.CartRepository
import com.fashionsaas.app.data.repository.ProductRepository
import com.fashionsaas.app.data.repository.StoreRepository
import com.fashionsaas.app.domain.model.Product
import com.fashionsaas.app.domain.model.ProductSize
import com.fashionsaas.app.domain.model.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para el catálogo de tienda.
 */
data class StoreCatalogUiState(
    val isLoading: Boolean = false,
    val store: Store? = null,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

/**
 * Estado de la UI para el detalle de producto.
 */
data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val selectedSize: ProductSize? = null,
    val quantity: Int = 1,
    val isAddedToCart: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para las pantallas de catálogo y detalle de producto.
 * Maneja la carga de productos, selección de talla y agregado al carrito.
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    /** Estado de la UI del catálogo de tienda. */
    private val _catalogState = MutableStateFlow(StoreCatalogUiState())
    val catalogState: StateFlow<StoreCatalogUiState> = _catalogState.asStateFlow()

    /** Estado de la UI del detalle de producto. */
    private val _detailState = MutableStateFlow(ProductDetailUiState())
    val detailState: StateFlow<ProductDetailUiState> = _detailState.asStateFlow()

    /**
     * Carga los datos de la tienda y sus productos.
     *
     * @param storeSlug slug de la tienda
     */
    fun loadStoreCatalog(storeSlug: String) {
        viewModelScope.launch {
            _catalogState.value = StoreCatalogUiState(isLoading = true)
            val storeResult = storeRepository.getStoreBySlug(storeSlug)
            val productsResult = productRepository.getStoreProducts(storeSlug)
            _catalogState.value = StoreCatalogUiState(
                isLoading = false,
                store = storeResult.getOrNull(),
                products = productsResult.getOrDefault(emptyList()),
                error = storeResult.exceptionOrNull()?.message
            )
        }
    }

    /**
     * Carga el detalle de un producto específico.
     *
     * @param storeSlug   slug de la tienda
     * @param productSlug slug del producto
     */
    fun loadProductDetail(storeSlug: String, productSlug: String) {
        viewModelScope.launch {
            _detailState.value = ProductDetailUiState(isLoading = true)
            val result = productRepository.getProductDetail(storeSlug, productSlug)
            result.fold(
                onSuccess = { product ->
                    _detailState.value = ProductDetailUiState(
                        isLoading = false,
                        product = product
                    )
                },
                onFailure = { e ->
                    _detailState.value = ProductDetailUiState(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    /**
     * Selecciona una talla para el producto actual.
     *
     * @param size talla seleccionada
     */
    fun selectSize(size: ProductSize) {
        _detailState.value = _detailState.value.copy(
            selectedSize = size,
            isAddedToCart = false
        )
    }

    /**
     * Actualiza la cantidad del producto a agregar al carrito.
     *
     * @param quantity nueva cantidad
     */
    fun updateQuantity(quantity: Int) {
        if (quantity < 1) return
        val maxStock = _detailState.value.selectedSize?.stock ?: 10
        if (quantity > maxStock) return
        _detailState.value = _detailState.value.copy(quantity = quantity)
    }

    /**
     * Agrega el producto actual al carrito con la talla y cantidad seleccionadas.
     * Requiere que se haya seleccionado una talla previamente.
     */
    fun addToCart() {
        val state = _detailState.value
        val product = state.product ?: return
        val size = state.selectedSize ?: run {
            _detailState.value = state.copy(error = "Por favor selecciona una talla.")
            return
        }
        viewModelScope.launch {
            cartRepository.addToCart(
                productId = product.id,
                productName = product.name,
                productPrice = product.finalPrice,
                imageUrl = product.imageUrl,
                sizeId = size.id,
                sizeName = size.size,
                storeId = product.storeId,
                storeName = product.storeName,
                quantity = state.quantity
            )
            _detailState.value = state.copy(isAddedToCart = true, error = null)
        }
    }

    /**
     * Limpia el error actual del estado de detalle.
     */
    fun clearError() {
        _detailState.value = _detailState.value.copy(error = null)
    }
}