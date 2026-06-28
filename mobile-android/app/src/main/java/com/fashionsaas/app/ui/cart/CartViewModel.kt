package com.fashionsaas.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashionsaas.app.data.repository.AuthRepository
import com.fashionsaas.app.data.repository.CartRepository
import com.fashionsaas.app.domain.model.CartItem
import com.fashionsaas.app.domain.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla del carrito.
 */
data class CartUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val isCheckingOut: Boolean = false,
    val orderCreated: Order? = null,
    val error: String? = null
) {
    /** Calcula el total del carrito sumando los subtotales de cada item. */
    val total: Double get() = cartItems.sumOf { it.subtotal }

    /** Indica si el carrito tiene items. */
    val isEmpty: Boolean get() = cartItems.isEmpty()
}

/**
 * ViewModel para la pantalla del carrito de compras.
 * Maneja la lista de items, actualización de cantidades y checkout.
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Estado de la UI del carrito. */
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    /** Flow que indica si el usuario está autenticado. */
    val isAuthenticated = authRepository.isAuthenticated

    init {
        observeCartItems()
    }

    /**
     * Observa los items del carrito en tiempo real usando Flow.
     * Se actualiza automáticamente cuando cambian los datos en Room.
     */
    private fun observeCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collect { items ->
                _uiState.value = _uiState.value.copy(cartItems = items)
            }
        }
    }

    /**
     * Actualiza la cantidad de un item en el carrito.
     * Si la cantidad es 0, elimina el item.
     *
     * @param cartItem   item a actualizar
     * @param newQuantity nueva cantidad
     */
    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity <= 0) {
                cartRepository.removeFromCart(cartItem)
            } else {
                cartRepository.updateQuantity(cartItem, newQuantity)
            }
        }
    }

    /**
     * Elimina un item específico del carrito.
     *
     * @param cartItem item a eliminar
     */
    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(cartItem)
        }
    }

    /**
     * Crea una orden con los items actuales del carrito.
     * Requiere que el usuario esté autenticado.
     *
     * @param shippingAddress dirección de envío
     * @param shippingCity    ciudad de envío
     * @param shippingPhone   teléfono de contacto
     */
    fun checkout(
        shippingAddress: String,
        shippingCity: String,
        shippingPhone: String
    ) {
        val items = _uiState.value.cartItems
        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "El carrito está vacío.")
            return
        }
        if (shippingAddress.isBlank() || shippingCity.isBlank() || shippingPhone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Todos los campos de envío son requeridos.")
            return
        }
        val storeId = items.first().storeId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingOut = true, error = null)
            val result = cartRepository.createOrder(storeId, shippingAddress, shippingCity, shippingPhone)
            result.fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingOut = false,
                        orderCreated = order,
                        cartItems = emptyList()
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingOut = false,
                        error = e.message
                    )
                }
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