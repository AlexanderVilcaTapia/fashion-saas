package com.fashionsaas.app.domain.model

/**
 * Modelo de dominio para una tienda.
 * Representa la entidad de negocio independiente de la fuente de datos.
 */
data class Store(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String?,
    val logoUrl: String?,
    val bannerUrl: String?,
    val address: String?,
    val city: String?,
    val phone: String?,
    val email: String?,
    val status: String,
    val totalProducts: Int,
    val ownerName: String
)