package com.fashionsaas.app.data.remote.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fashionsaas.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Authenticator de OkHttp que maneja automáticamente la renovación del token JWT.
 * Cuando una petición recibe un 401, intenta refrescar el access token usando
 * el refresh token almacenado en DataStore y reintenta la petición original.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : Authenticator {

    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    /**
     * Intercepta respuestas 401 y renueva el token automáticamente.
     * Si el refresh falla, retorna null para cancelar la petición.
     *
     * @param route   ruta de la petición
     * @param response respuesta 401 recibida
     * @return nueva petición con token renovado o null si falla
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // Evitar bucle infinito si ya reintentamos
        if (response.request.header("X-Retry") != null) return null

        val refreshToken = runBlocking {
            dataStore.data.first()[REFRESH_TOKEN_KEY]
        } ?: return null

        // Intentar refrescar el token
        val newAccessToken = runBlocking {
            try {
                val client = okhttp3.OkHttpClient()
                val body = "{\"refresh\":\"$refreshToken\"}"
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("${getBaseUrl()}/api/auth/refresh/")
                    .post(body)
                    .build()

                val refreshResponse = client.newCall(request).execute()
                if (refreshResponse.isSuccessful) {
                    val responseBody = refreshResponse.body?.string()
                    val json = JSONObject(responseBody ?: "")
                    val newToken = json.getString("access")
                    dataStore.edit { prefs ->
                        prefs[ACCESS_TOKEN_KEY] = newToken
                    }
                    newToken
                } else {
                    // Refresh falló, limpiar tokens
                    dataStore.edit { prefs ->
                        prefs.remove(ACCESS_TOKEN_KEY)
                        prefs.remove(REFRESH_TOKEN_KEY)
                    }
                    null
                }
            } catch (e: Exception) {
                null
            }
        } ?: return null

        // Reintentar la petición original con el nuevo token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .header("X-Retry", "true")
            .build()
    }

    /**
     * Obtiene la URL base de la API desde BuildConfig.
     */
    private fun getBaseUrl(): String {
        return com.fashionsaas.app.BuildConfig.DJANGO_BASE_URL
    }
}