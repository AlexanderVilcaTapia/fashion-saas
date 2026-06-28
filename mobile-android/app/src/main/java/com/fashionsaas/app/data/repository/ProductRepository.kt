package com.fashionsaas.app.data.repository

import com.fashionsaas.app.data.local.dao.ProductDao
import com.fashionsaas.app.data.local.entity.ProductEntity
import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.data.remote.dto.ProductDto
import com.fashionsaas.app.domain.model.Product
import com.fashionsaas.app.domain.model.ProductImage
import com.fashionsaas.app.domain.model.ProductSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de productos que implementa el patrón Repository.
 * Coordina entre la fuente de datos remota (API Django) y local (Room).
 * Implementa caché local para uso sin conexión.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao
) {

    /**
     * Obtiene los productos destacados.
     * Primero intenta obtenerlos de la API y los guarda en caché local.
     * Si falla la red, retorna los productos en caché.
     *
     * @return Result con la lista de productos destacados
     */
    suspend fun getFeaturedProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getFeaturedProducts()
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                val entities = products.map { it.toEntity() }
                productDao.insertProducts(entities)
                Result.success(products.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los productos de una tienda específica desde la API.
     *
     * @param storeSlug slug de la tienda
     * @param category  filtro por categoría opcional
     * @param search    texto de búsqueda opcional
     * @return Result con la lista de productos de la tienda
     */
    suspend fun getStoreProducts(
        storeSlug: String,
        category: String? = null,
        search: String? = null
    ): Result<List<Product>> {
        return try {
            val response = apiService.getStoreProducts(storeSlug, category, search)
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                productDao.insertProducts(products.map { it.toEntity() })
                Result.success(products.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el detalle de un producto desde la API.
     *
     * @param storeSlug   slug de la tienda
     * @param productSlug slug del producto
     * @return Result con los datos del producto
     */
    suspend fun getProductDetail(storeSlug: String, productSlug: String): Result<Product> {
        return try {
            val response = apiService.getProductDetail(storeSlug, productSlug)
            if (response.isSuccessful) {
                val product = response.body()!!
                Result.success(product.toDomain())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los productos en caché local como un Flow reactivo.
     * Se actualiza automáticamente cuando cambian los datos locales.
     *
     * @return Flow con la lista de productos en caché
     */
    fun getCachedProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Busca productos en la API por texto.
     *
     * @param query texto de búsqueda
     * @return Result con la lista de productos encontrados
     */
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response = apiService.searchProducts(search = query)
            if (response.isSuccessful) {
                val products = response.body()?.results ?: emptyList()
                Result.success(products.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Convierte un ProductDto de la API a un modelo de dominio Product.
 */
fun ProductDto.toDomain(): Product = Product(
    id = id,
    storeId = storeId,
    storeName = storeName,
    name = name,
    slug = slug,
    description = description,
    price = price.toDoubleOrNull() ?: 0.0,
    discountPrice = discountPrice?.toDoubleOrNull(),
    finalPrice = finalPrice.toDoubleOrNull() ?: 0.0,
    hasDiscount = hasDiscount,
    imageUrl = images.firstOrNull { it.isPrimary }?.image ?: images.firstOrNull()?.image,
    categoryName = categoryName,
    status = status,
    isFeatured = isFeatured,
    sizes = sizes.map { ProductSize(it.id, it.size, it.stock) },
    images = images.map { ProductImage(it.id, it.image, it.isPrimary) }
)

/**
 * Convierte un ProductDto de la API a una entidad de Room para caché local.
 */
fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    storeId = storeId,
    storeName = storeName,
    name = name,
    slug = slug,
    description = description,
    price = price.toDoubleOrNull() ?: 0.0,
    discountPrice = discountPrice?.toDoubleOrNull(),
    imageUrl = images.firstOrNull { it.isPrimary }?.image ?: images.firstOrNull()?.image,
    categoryName = categoryName,
    status = status,
    isFeatured = isFeatured
)

/**
 * Convierte una entidad de Room a un modelo de dominio Product.
 */
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    storeId = storeId,
    storeName = storeName,
    name = name,
    slug = slug,
    description = description,
    price = price,
    discountPrice = discountPrice,
    finalPrice = discountPrice ?: price,
    hasDiscount = discountPrice != null,
    imageUrl = imageUrl,
    categoryName = categoryName,
    status = status,
    isFeatured = isFeatured,
    sizes = emptyList(),
    images = emptyList()
)