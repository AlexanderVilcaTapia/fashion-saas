package com.fashionsaas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la petición de login a la API Django.
 */
data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * DTO para la petición de registro a la API Django.
 */
data class RegisterRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("password") val password: String,
    @SerializedName("password2") val password2: String,
    @SerializedName("role") val role: String = "buyer"
)

/**
 * DTO para deserializar la respuesta de login de la API Django.
 */
data class LoginResponseDto(
    @SerializedName("refresh") val refresh: String,
    @SerializedName("access") val access: String
)

/**
 * DTO para deserializar la respuesta de registro de la API Django.
 */
data class RegisterResponseDto(
    @SerializedName("user") val user: UserDto,
    @SerializedName("tokens") val tokens: TokensDto
)

/**
 * DTO para deserializar los tokens JWT de la API Django.
 */
data class TokensDto(
    @SerializedName("refresh") val refresh: String,
    @SerializedName("access") val access: String
)

/**
 * DTO para deserializar los datos del usuario autenticado.
 */
data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("role") val role: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar") val avatar: String?
)