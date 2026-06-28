package com.fashionsaas.app.data.repository

import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.data.remote.dto.StoreDto
import com.fashionsaas.app.domain.model.Store
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de tiendas.
 * Consume la API Django para obtener tiendas destacadas y búsqueda.
 */
@Singleton
class StoreRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Obtiene las tiendas destacadas para la pantalla principal.
     *
     * @return Result con la lista de tiendas destacadas
     */
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

    /**
     * Obtiene la lista de tiendas activas con filtros opcionales.
     *
     * @param search texto de búsqueda opcional
     * @param city   filtro por ciudad opcional
     * @return Result con la lista de tiendas
     */
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

    /**
     * Obtiene el detalle de una tienda por su slug.
     *
     * @param slug identificador único de la tienda
     * @return Result con los datos de la tienda
     */
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
 */
fun StoreDto.toDomain(): Store = Store(
    id = id,
    name = name,
    slug = slug,
    description = description,
    logoUrl = logo,
    bannerUrl = banner,
    address = address,
    city = city,
    phone = phone,
    email = email,
    status = status,
    totalProducts = totalProducts,
    ownerName = ownerName
)