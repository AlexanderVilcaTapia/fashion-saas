package com.fashionsaas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para almacenar productos en caché local.
 * Permite navegar el catálogo sin conexión a internet.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Int,

    /** Identificador de la tienda a la que pertenece el producto. */
    val storeId: Int,

    /** Nombre de la tienda. */
    val storeName: String,

    /** Nombre del producto. */
    val name: String,

    /** Slug único del producto para navegación. */
    val slug: String,

    /** Descripción detallada del producto. */
    val description: String?,

    /** Precio original del producto. */
    val price: Double,

    /** Precio con descuento, si aplica. */
    val discountPrice: Double?,

    /** URL de la imagen principal del producto. */
    val imageUrl: String?,

    /** Nombre de la categoría del producto. */
    val categoryName: String?,

    /** Estado del producto (active, inactive, out_of_stock). */
    val status: String,

    /** Indica si el producto es destacado. */
    val isFeatured: Boolean = false,

    /** Timestamp de la última sincronización con la API. */
    val lastSynced: Long = System.currentTimeMillis()
)