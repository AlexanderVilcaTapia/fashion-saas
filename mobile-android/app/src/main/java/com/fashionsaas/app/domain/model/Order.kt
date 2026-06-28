package com.fashionsaas.app.domain.model

/**
 * Modelo de dominio para una orden de compra.
 */
data class Order(
    val id: Int,
    val buyerName: String,
    val storeName: String,
    val status: String,
    val paymentStatus: String,
    val subtotal: Double,
    val shippingCost: Double,
    val total: Double,
    val shippingAddress: String,
    val shippingCity: String,
    val shippingPhone: String,
    val createdAt: String,
    val items: List<OrderItem>
)

/**
 * Modelo de dominio para un item dentro de una orden.
 */
data class OrderItem(
    val id: Int,
    val productName: String,
    val sizeName: String?,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)