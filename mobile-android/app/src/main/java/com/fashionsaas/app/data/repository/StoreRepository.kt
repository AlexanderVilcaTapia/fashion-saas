package com.fashionsaas.app.data.repository

import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.data.remote.dto.StoreDto
import com.fashionsaas.app.domain.model.Store
import javax.inject.Inject
import javax.inject.Singleton

private const val MEDIA_BASE_URL = "https://fashion-saas-production.up.railway.app"

/**
 * Convierte una ruta de imagen relativa en una URL absoluta completa.
 *
 * @param path ruta de la imagen devuelta por la API
 * @return URL absoluta completa de la imagen
 */
private fun resolveImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http")) path else "$MEDIA_BASE_URL$path"
}

/**
 * Repositorio de tiendas.
 * Consume la API Django para obtener tiendas destacadas y búsqueda.
 */
@Singleton
class StoreRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getFeaturedStores(): Result<List<Store>> {
        return try {
            val response = apiService.getFeaturedStores()
            if (response.isSuccessful) {
                val stores = response.body() ?: emptyList()
                Result.success(stores.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getStores(
        search: String? = null,
        city: String? = null
    ): Result<List<Store>> {
        return try {
            val response = apiService.getStores(search, city)
            if (response.isSuccessful) {
                val stores = response.body() ?: emptyList()
                Result.success(stores.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getStoreBySlug(slug: String): Result<Store> {
        return try {
            val response = apiService.getStoreBySlug(slug)
            if (response.isSuccessful) {
                val store = response.body()!!
                Result.success(store.toDomain())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}

/**
 * Convierte un StoreDto de la API a un modelo de dominio Store.
 * Resuelve las URLs de logo y banner a rutas absolutas.
 */
fun StoreDto.toDomain(): Store = Store(
    id = id,
    name = name,
    slug = slug,
    description = description,
    logoUrl = resolveImageUrl(logo),
    bannerUrl = resolveImageUrl(banner),
    address = address,
    city = city,
    phone = phone,
    email = email,
    status = status,
    totalProducts = totalProducts,
    ownerName = ownerName,
    latitude = latitude?.toDoubleOrNull(),
    longitude = longitude?.toDoubleOrNull()
)