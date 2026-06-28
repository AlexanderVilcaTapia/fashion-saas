package com.fashionsaas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para deserializar la respuesta de productos de la API Django.
 */
data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("store") val storeId: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("category") val categoryId: Int?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: String,
    @SerializedName("discount_price") val discountPrice: String?,
    @SerializedName("final_price") val finalPrice: String,
    @SerializedName("has_discount") val hasDiscount: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("is_featured") val isFeatured: Boolean,
    @SerializedName("sizes") val sizes: List<ProductSizeDto>,
    @SerializedName("images") val images: List<ProductImageDto>
)

/**
 * DTO para deserializar las tallas de un producto.
 */
data class ProductSizeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("size") val size: String,
    @SerializedName("stock") val stock: Int
)

/**
 * DTO para deserializar las imágenes de un producto.
 */
data class ProductImageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("image") val image: String,
    @SerializedName("is_primary") val isPrimary: Boolean,
    @SerializedName("order") val order: Int
)

/**
 * DTO para deserializar la respuesta paginada de productos.
 */
data class ProductListDto(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<ProductDto>
)