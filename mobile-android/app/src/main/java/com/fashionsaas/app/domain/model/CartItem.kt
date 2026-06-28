package com.fashionsaas.app.domain.model

/**
 * Modelo de dominio para un item del carrito de compras.
 */
data class CartItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String?,
    val sizeId: Int,
    val sizeName: String,
    val quantity: Int,
    val storeId: Int,
    val storeName: String
) {
    /** Calcula el subtotal del item multiplicando precio por cantidad. */
    val subtotal: Double get() = productPrice * quantity
}