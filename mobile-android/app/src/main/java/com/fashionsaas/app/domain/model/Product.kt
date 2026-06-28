package com.fashionsaas.app.domain.model

/**
 * Modelo de dominio para un producto.
 * Representa la entidad de negocio independiente de la fuente de datos.
 */
data class Product(
    val id: Int,
    val storeId: Int,
    val storeName: String,
    val name: String,
    val slug: String,
    val description: String?,
    val price: Double,
    val discountPrice: Double?,
    val finalPrice: Double,
    val hasDiscount: Boolean,
    val imageUrl: String?,
    val categoryName: String?,
    val status: String,
    val isFeatured: Boolean,
    val sizes: List<ProductSize>,
    val images: List<ProductImage>
)

/**
 * Modelo de dominio para una talla de producto.
 */
data class ProductSize(
    val id: Int,
    val size: String,
    val stock: Int
)

/**
 * Modelo de dominio para una imagen de producto.
 */
data class ProductImage(
    val id: Int,
    val imageUrl: String,
    val isPrimary: Boolean
)