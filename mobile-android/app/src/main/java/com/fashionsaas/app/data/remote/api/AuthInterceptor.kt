package com.fashionsaas.app.data.remote.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que agrega el token JWT a cada petición HTTP.
 * Se inyecta en el cliente Retrofit para autenticar automáticamente.
 *
 * @param tokenProvider función que provee el token JWT actual
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    /**
     * Intercepta cada petición y agrega el header Authorization si hay token disponible.
     *
     * @param chain cadena de interceptores de OkHttp
     * @return respuesta HTTP
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}