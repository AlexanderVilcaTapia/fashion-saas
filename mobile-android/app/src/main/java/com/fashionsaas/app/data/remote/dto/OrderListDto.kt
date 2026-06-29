package com.fashionsaas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para deserializar la respuesta paginada de órdenes de la API Django.
 */
data class OrderListDto(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<OrderDto>
)