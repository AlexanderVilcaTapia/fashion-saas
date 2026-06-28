package com.fashionsaas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fashionsaas.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos relacionadas a productos.
 * Provee caché local del catálogo para uso sin conexión.
 */
@Dao
interface ProductDao {

    /**
     * Inserta o actualiza una lista de productos en la base de datos local.
     * Si el producto ya existe, lo reemplaza con los datos más recientes.
     *
     * @param products lista de productos a insertar o actualizar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    /**
     * Obtiene todos los productos activos como un Flow reactivo.
     * Se actualiza automáticamente cuando cambian los datos.
     *
     * @return Flow con la lista de productos activos
     */
    @Query("SELECT * FROM products WHERE status = 'active' ORDER BY isFeatured DESC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    /**
     * Obtiene los productos de una tienda específica.
     *
     * @param storeId identificador de la tienda
     * @return Flow con la lista de productos de la tienda
     */
    @Query("SELECT * FROM products WHERE storeId = :storeId AND status = 'active' ORDER BY name ASC")
    fun getProductsByStore(storeId: Int): Flow<List<ProductEntity>>

    /**
     * Obtiene los productos destacados para mostrar en la pantalla principal.
     *
     * @return Flow con la lista de productos destacados
     */
    @Query("SELECT * FROM products WHERE isFeatured = 1 AND status = 'active' LIMIT 10")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    /**
     * Busca productos por nombre.
     *
     * @param query texto de búsqueda
     * @return Flow con los productos que coinciden con la búsqueda
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND status = 'active'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    /**
     * Obtiene un producto específico por su identificador.
     *
     * @param productId identificador del producto
     * @return producto encontrado o null si no existe
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    /**
     * Elimina todos los productos del caché local.
     * Se usa para forzar una resincronización con la API.
     */
    @Query("DELETE FROM products")
    suspend fun clearProducts()
}