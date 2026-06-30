package com.fashionsaas.app.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.domain.model.Store
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla del mapa de tiendas.
 */
data class StoreMapUiState(
    val isLoading: Boolean = false,
    val stores: List<Store> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla del mapa de tiendas.
 * Carga las tiendas desde la API para mostrarlas en el mapa.
 */
@HiltViewModel
class StoreMapViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreMapUiState())
    val uiState: StateFlow<StoreMapUiState> = _uiState.asStateFlow()

    init {
        loadStores()
    }

    /**
     * Carga las tiendas activas desde la API de Django.
     */
    fun loadStores() {
        viewModelScope.launch {
            _uiState.value = StoreMapUiState(isLoading = true)
            try {
                val response = apiService.getFeaturedStores()
                if (response.isSuccessful) {
                    val stores = response.body()?.map { dto ->
                        Store(
                            id = dto.id,
                            name = dto.name,
                            slug = dto.slug,
                            description = dto.description,
                            logoUrl = dto.logo,
                            bannerUrl = dto.banner,
                            address = dto.address,
                            city = dto.city,
                            phone = dto.phone,
                            email = dto.email,
                            status = dto.status,
                            totalProducts = dto.totalProducts,
                            ownerName = dto.ownerName,
                            latitude = dto.latitude?.toDoubleOrNull(),
                            longitude = dto.longitude?.toDoubleOrNull()
                        )
                    } ?: emptyList()
                    _uiState.value = StoreMapUiState(stores = stores)
                } else {
                    _uiState.value = StoreMapUiState(error = "Error al cargar tiendas.")
                }
            } catch (e: Exception) {
                _uiState.value = StoreMapUiState(error = "Error de conexión: ${e.message}")
            }
        }
    }
}

/**
 * Pantalla del mapa de tiendas usando Google Maps SDK.
 * Muestra las tiendas como marcadores en el mapa centrado en Arequipa, Perú.
 * Permite navegar al catálogo de cada tienda al hacer click en el marcador.
 *
 * @param onBackClick  callback para volver atrás
 * @param onStoreClick callback cuando se selecciona una tienda en el mapa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreMapScreen(
    onBackClick: () -> Unit,
    onStoreClick: (String) -> Unit,
    viewModel: StoreMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Posición inicial del mapa centrada en Arequipa, Perú
    val arequipa = LatLng(-16.4090, -71.5375)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipa, 13f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tiendas cerca de ti") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    uiState.stores.forEach { store ->
                        if (store.latitude != null && store.longitude != null) {
                            val storePosition = LatLng(store.latitude, store.longitude)
                            Marker(
                                state = MarkerState(position = storePosition),
                                title = store.name,
                                snippet = store.address ?: store.city ?: "Ver tienda",
                                onClick = {
                                    onStoreClick(store.slug)
                                    true
                                }
                            )
                        }
                    }
                }

                // Card informativo superpuesto
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${uiState.stores.size} tiendas disponibles",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toca un marcador para ver la tienda",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}