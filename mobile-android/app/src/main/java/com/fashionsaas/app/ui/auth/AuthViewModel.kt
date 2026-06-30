package com.fashionsaas.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashionsaas.app.data.repository.AuthRepository
import com.fashionsaas.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para las pantallas de autenticación.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

/**
 * ViewModel para las pantallas de login y registro.
 * Maneja la lógica de autenticación y expone el estado via StateFlow.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cartRepository: com.fashionsaas.app.data.repository.CartRepository
) : ViewModel() {

    /** Estado de la UI de autenticación. */
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Flow que indica si el usuario está autenticado. */
    val isAuthenticated = authRepository.isAuthenticated

    /**
     * Inicia sesión con email y contraseña.
     * Actualiza el estado de la UI según el resultado.
     *
     * @param email    correo del usuario
     * @param password contraseña del usuario
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Email y contraseña son requeridos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    val userResult = authRepository.getCurrentUser()
                    _uiState.value = AuthUiState(
                        isSuccess = true,
                        user = userResult.getOrNull()
                    )
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message)
                }
            )
        }
    }

    /**
     * Registra un nuevo usuario comprador.
     * Actualiza el estado de la UI según el resultado.
     *
     * @param email     correo del nuevo usuario
     * @param username  nombre de usuario
     * @param firstName nombre
     * @param lastName  apellido
     * @param password  contraseña
     * @param password2 confirmación de contraseña
     */
    fun register(
        email: String,
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        password2: String
    ) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _uiState.value = AuthUiState(error = "Todos los campos son requeridos.")
            return
        }
        if (password != password2) {
            _uiState.value = AuthUiState(error = "Las contraseñas no coinciden.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.register(
                email, username, firstName, lastName, password, password2
            )
            result.fold(
                onSuccess = { _uiState.value = AuthUiState(isSuccess = true) },
                onFailure = { e -> _uiState.value = AuthUiState(error = e.message) }
            )
        }
    }

    /**
     * Cierra la sesión del usuario actual y limpia el carrito local.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            cartRepository.clearLocalCart()
            _uiState.value = AuthUiState()
        }
    }

    /**
     * Limpia el error actual del estado de la UI.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}