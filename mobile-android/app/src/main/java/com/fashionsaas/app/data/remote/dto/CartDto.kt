package com.fashionsaas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para agregar un item al carrito en Django.
 */
data class CartItemAddDto(
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("size_id") val sizeId: Int,
    @SerializedName("quantity") val quantity: Int
)