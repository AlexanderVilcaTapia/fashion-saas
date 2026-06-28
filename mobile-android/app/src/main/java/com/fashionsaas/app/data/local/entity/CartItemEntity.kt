package com.fashionsaas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para almacenar items del carrito localmente.
 * El carrito persiste aunque el usuario cierre la app.
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Identificador del producto agregado al carrito. */
    val productId: Int,

    /** Nombre del producto. */
    val productName: String,

    /** Precio unitario del producto. */
    val productPrice: Double,

    /** URL de la imagen del producto. */
    val productImageUrl: String?,

    /** Identificador de la talla seleccionada. */
    val sizeId: Int,

    /** Nombre de la talla seleccionada (XS, S, M, L, XL). */
    val sizeName: String,

    /** Cantidad de unidades en el carrito. */
    val quantity: Int,

    /** Identificador de la tienda del producto. */
    val storeId: Int,

    /** Nombre de la tienda del producto. */
    val storeName: String
)