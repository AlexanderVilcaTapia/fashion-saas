package com.fashionsaas.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashionsaas.app.BuildConfig
import com.fashionsaas.app.data.repository.CartRepository
import com.fashionsaas.app.domain.model.CartItem
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de recomendaciones de IA.
 */
data class AiUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val recommendation: String = "",
    val error: String? = null
)

/**
 * ViewModel para la pantalla de recomendaciones de outfits con Gemini AI.
 * Analiza los productos del carrito y genera recomendaciones personalizadas.
 * La IA está integrada en el flujo central de la app, no como función extra.
 */
@HiltViewModel
class AiViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    /** Estado de la UI de recomendaciones de IA. */
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    /**
     * Modelo generativo de Gemini configurado para recomendaciones de moda.
     */
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 500
        }
    )

    init {
        loadCartAndRecommend()
    }

    /**
     * Carga los items del carrito y genera recomendaciones automáticamente.
     * Este es el flujo central: la IA se activa con los datos reales del carrito.
     */
    fun loadCartAndRecommend() {
        viewModelScope.launch {
            _uiState.value = AiUiState(isLoading = true)
            try {
                val items = cartRepository.getCartItems().first()
                _uiState.value = _uiState.value.copy(cartItems = items)
                if (items.isNotEmpty()) {
                    generateOutfitRecommendation(items)
                } else {
                    _uiState.value = AiUiState(
                        isLoading = false,
                        recommendation = "Agrega productos a tu carrito para recibir recomendaciones de outfits personalizadas."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AiUiState(
                    isLoading = false,
                    error = "Error al cargar el carrito: ${e.message}"
                )
            }
        }
    }

    /**
     * Genera recomendaciones de outfits usando Gemini AI basándose en los
     * productos reales del carrito del usuario.
     *
     * @param items lista de items del carrito con datos reales
     */
    private suspend fun generateOutfitRecommendation(items: List<CartItem>) {
        try {
            val productsList = items.joinToString(", ") {
                "${it.productName} (talla ${it.sizeName})"
            }
            val prompt = """
                Eres un experto en moda y estilismo. El usuario tiene estos productos en su carrito de una tienda de ropa:
                $productsList
                
                Por favor:
                1. Analiza los productos y crea 2-3 combinaciones de outfits completos con estas prendas.
                2. Sugiere qué accesorios complementarían cada outfit.
                3. Indica para qué ocasiones son ideales estos outfits.
                4. Da un consejo de cuidado para estas prendas.
                
                Responde en español de forma amigable y concisa.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val recommendation = response.text ?: "No se pudo generar una recomendación."

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                recommendation = recommendation,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Error al generar recomendaciones: ${e.message}"
            )
        }
    }

    /**
     * Regenera las recomendaciones con los datos actuales del carrito.
     */
    fun regenerateRecommendation() {
        loadCartAndRecommend()
    }

    /**
     * Limpia el error actual del estado de la UI.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}