package com.fashionsaas.app.domain.model

/**
 * Modelo de dominio para el usuario autenticado.
 */
data class User(
    val id: Int,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val role: String,
    val phone: String?,
    val avatarUrl: String?
)