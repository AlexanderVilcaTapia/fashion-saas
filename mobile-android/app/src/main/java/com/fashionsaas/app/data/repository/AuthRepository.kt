package com.fashionsaas.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.data.remote.dto.LoginRequestDto
import com.fashionsaas.app.data.remote.dto.RegisterRequestDto
import com.fashionsaas.app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de autenticación.
 * Gestiona el login, registro y persistencia del token JWT en DataStore.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        /** Clave para el token de acceso JWT en DataStore. */
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")

        /** Clave para el token de refresco JWT en DataStore. */
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

        /** Clave para el email del usuario en DataStore. */
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    /**
     * Flow que emite true si el usuario está autenticado, false en caso contrario.
     */
    val isAuthenticated: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[ACCESS_TOKEN_KEY].isNullOrEmpty()
    }

    /**
     * Inicia sesión con email y contraseña contra la API Django.
     * Guarda los tokens JWT en DataStore si el login es exitoso.
     *
     * @param email    correo del usuario
     * @param password contraseña del usuario
     * @return Result con el mensaje de éxito o error
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                val tokens = response.body()!!
                saveTokens(tokens.access, tokens.refresh)
                Result.success("Login exitoso")
            } else {
                Result.failure(Exception("Credenciales incorrectas."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        }
    }

    /**
     * Registra un nuevo usuario comprador en la API Django.
     * Guarda los tokens JWT en DataStore si el registro es exitoso.
     *
     * @param email     correo del nuevo usuario
     * @param username  nombre de usuario
     * @param firstName nombre del usuario
     * @param lastName  apellido del usuario
     * @param password  contraseña
     * @param password2 confirmación de contraseña
     * @return Result con el mensaje de éxito o error
     */
    suspend fun register(
        email: String,
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        password2: String
    ): Result<String> {
        return try {
            val response = apiService.register(
                RegisterRequestDto(email, username, firstName, lastName, password, password2)
            )
            if (response.isSuccessful) {
                val data = response.body()!!
                saveTokens(data.tokens.access, data.tokens.refresh)
                Result.success("Registro exitoso")
            } else {
                Result.failure(Exception("Error al registrarse. El email puede estar en uso."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        }
    }

    /**
     * Obtiene los datos del usuario autenticado desde la API.
     *
     * @return Result con los datos del usuario
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                val userDto = response.body()!!
                val user = User(
                    id = userDto.id,
                    email = userDto.email,
                    username = userDto.username,
                    firstName = userDto.firstName,
                    lastName = userDto.lastName,
                    fullName = userDto.fullName,
                    role = userDto.role,
                    phone = userDto.phone,
                    avatarUrl = userDto.avatar
                )
                dataStore.edit { prefs ->
                    prefs[USER_EMAIL_KEY] = user.email
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Error al obtener el usuario."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión del usuario eliminando los tokens de DataStore.
     */
    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_EMAIL_KEY)
        }
    }

    /**
     * Obtiene el token de acceso actual desde DataStore.
     *
     * @return token de acceso o null si no está autenticado
     */
    suspend fun getAccessToken(): String? {
        return dataStore.data.first()[ACCESS_TOKEN_KEY]
    }

    /**
     * Guarda los tokens JWT en DataStore.
     *
     * @param accessToken  token de acceso
     * @param refreshToken token de refresco
     */
    private suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    /**
     * Autentica al usuario usando un token de Firebase obtenido de Google Sign-In.
     * Envía el token a Django para verificarlo y obtener los JWT del sistema.
     *
     * @param firebaseToken token de ID de Firebase tras autenticarse con Google
     * @return Result con el mensaje de éxito o error
     */
    suspend fun loginWithGoogle(firebaseToken: String): Result<String> {
        return try {
            val response = apiService.googleLogin(
                com.fashionsaas.app.data.remote.dto.GoogleLoginRequestDto(firebaseToken)
            )
            if (response.isSuccessful) {
                val data = response.body()!!
                saveTokens(data.access, data.refresh)
                dataStore.edit { prefs ->
                    prefs[USER_EMAIL_KEY] = data.user.email
                }
                Result.success("Login con Google exitoso")
            } else {
                Result.failure(Exception("Error al iniciar sesión con Google."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        }
    }
}
