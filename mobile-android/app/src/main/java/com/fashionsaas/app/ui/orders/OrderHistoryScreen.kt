package com.fashionsaas.app.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.fashionsaas.app.domain.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para el historial de órdenes.
 */
data class OrderHistoryUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla de historial de órdenes.
 */
@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Carga el historial de órdenes del usuario autenticado.
     */
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrderHistoryUiState(isLoading = true)
            try {
                val response = apiService.getOrders()
                if (response.isSuccessful) {
                    val orders = response.body()?.map { dto ->
                        Order(
                            id = dto.id,
                            buyerName = dto.buyerName,
                            storeName = dto.storeName,
                            status = dto.status,
                            paymentStatus = dto.paymentStatus,
                            subtotal = dto.subtotal.toDoubleOrNull() ?: 0.0,
                            shippingCost = dto.shippingCost.toDoubleOrNull() ?: 0.0,
                            total = dto.total.toDoubleOrNull() ?: 0.0,
                            shippingAddress = dto.shippingAddress,
                            shippingCity = dto.shippingCity,
                            shippingPhone = dto.shippingPhone,
                            createdAt = dto.createdAt,
                            items = emptyList()
                        )
                    } ?: emptyList()
                    _uiState.value = OrderHistoryUiState(orders = orders)
                } else {
                    _uiState.value = OrderHistoryUiState(error = "Error al cargar órdenes.")
                }
            } catch (e: Exception) {
                _uiState.value = OrderHistoryUiState(error = "Error de conexión: ${e.message}")
            }
        }
    }
}

/**
 * Pantalla del historial de órdenes del comprador.
 * Muestra todas las órdenes con su estado y total.
 *
 * @param onBackClick callback para volver atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis órdenes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.orders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes órdenes aún",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tus compras aparecerán aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.orders) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de orden para mostrar en el historial.
 *
 * @param order datos de la orden
 */
@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Orden #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = order.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.storeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = order.createdAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "S/. ${String.format("%.2f", order.total)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Chip de estado de orden con color según el estado.
 *
 * @param status estado de la orden
 */
@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "confirmed" -> MaterialTheme.colorScheme.primary
        "shipped" -> MaterialTheme.colorScheme.secondary
        "delivered" -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        "pending" -> "Pendiente"
        "confirmed" -> "Confirmado"
        "shipped" -> "Enviado"
        "delivered" -> "Entregado"
        "cancelled" -> "Cancelado"
        else -> status
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold
    )
}