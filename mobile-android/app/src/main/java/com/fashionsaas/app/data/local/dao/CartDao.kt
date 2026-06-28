package com.fashionsaas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fashionsaas.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos relacionadas al carrito de compras.
 * El carrito se almacena localmente para persistir entre sesiones.
 */
@Dao
interface CartDao {

    /**
     * Inserta un item al carrito local.
     *
     * @param cartItem item a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    /**
     * Actualiza un item existente en el carrito.
     *
     * @param cartItem item con los datos actualizados
     */
    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    /**
     * Elimina un item específico del carrito.
     *
     * @param cartItem item a eliminar
     */
    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    /**
     * Obtiene todos los items del carrito como un Flow reactivo.
     * Se actualiza automáticamente cuando cambian los datos.
     *
     * @return Flow con la lista de items del carrito
     */
    @Query("SELECT * FROM cart_items ORDER BY id DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    /**
     * Obtiene el número total de items en el carrito.
     *
     * @return Flow con el conteo de items
     */
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCount(): Flow<Int>

    /**
     * Elimina todos los items del carrito.
     * Se usa después de completar una orden.
     */
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    /**
     * Verifica si un producto con una talla específica ya está en el carrito.
     *
     * @param productId identificador del producto
     * @param sizeId    identificador de la talla
     * @return item encontrado o null si no existe
     */
    @Query("SELECT * FROM cart_items WHERE productId = :productId AND sizeId = :sizeId LIMIT 1")
    suspend fun getCartItemByProductAndSize(productId: Int, sizeId: Int): CartItemEntity?
}