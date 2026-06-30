package com.fashionsaas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para deserializar la respuesta de tiendas de la API Django.
 */
data class StoreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("logo") val logo: String?,
    @SerializedName("banner") val banner: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("status") val status: String,
    @SerializedName("total_products") val totalProducts: Int,
    @SerializedName("owner_name") val ownerName: String,
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?
)