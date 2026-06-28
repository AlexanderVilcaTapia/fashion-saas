package com.fashionsaas.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fashionsaas.app.data.local.dao.CartDao
import com.fashionsaas.app.data.local.dao.ProductDao
import com.fashionsaas.app.data.local.entity.CartItemEntity
import com.fashionsaas.app.data.local.entity.ProductEntity

/**
 * Base de datos local de Fashion SaaS usando Room.
 * Almacena productos en caché y el carrito de compras del usuario.
 * Versión 1 — entidades: ProductEntity, CartItemEntity.
 */
@Database(
    entities = [ProductEntity::class, CartItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FashionDatabase : RoomDatabase() {

    /**
     * DAO para operaciones sobre la tabla de productos.
     */
    abstract fun productDao(): ProductDao

    /**
     * DAO para operaciones sobre la tabla del carrito.
     */
    abstract fun cartDao(): CartDao

    companion object {
        /** Nombre de la base de datos en el dispositivo. */
        const val DATABASE_NAME = "fashion_saas_db"
    }
}