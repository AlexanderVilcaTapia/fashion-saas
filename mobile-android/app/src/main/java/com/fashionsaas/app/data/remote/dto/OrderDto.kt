package com.fashionsaas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la petición de creación de orden a la API Django.
 */
data class CreateOrderRequestDto(
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("shipping_address") val shippingAddress: String,
    @SerializedName("shipping_city") val shippingCity: String,
    @SerializedName("shipping_phone") val shippingPhone: String,
    @SerializedName("notes") val notes: String = ""
)

/**
 * DTO para deserializar la respuesta de orden de la API Django.
 */
data class OrderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("buyer_name") val buyerName: String,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("status") val status: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("subtotal") val subtotal: String,
    @SerializedName("shipping_cost") val shippingCost: String,
    @SerializedName("total") val total: String,
    @SerializedName("shipping_address") val shippingAddress: String,
    @SerializedName("shipping_city") val shippingCity: String,
    @SerializedName("shipping_phone") val shippingPhone: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("items") val items: List<OrderItemDto>
)

/**
 * DTO para deserializar los items de una orden.
 */
data class OrderItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("size_name") val sizeName: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: String,
    @SerializedName("subtotal") val subtotal: String
)