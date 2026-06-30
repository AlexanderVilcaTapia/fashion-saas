package com.fashionsaas.app.data.repository

import com.fashionsaas.app.data.local.dao.CartDao
import com.fashionsaas.app.data.local.entity.CartItemEntity
import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.data.remote.dto.CreateOrderRequestDto
import com.fashionsaas.app.domain.model.CartItem
import com.fashionsaas.app.domain.model.Order
import com.fashionsaas.app.domain.model.OrderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.fashionsaas.app.data.remote.dto.CartItemAddDto
import kotlinx.coroutines.flow.first

/**
 * Repositorio del carrito de compras.
 * Gestiona el carrito local con Room y la creación de órdenes en la API Django.
 */
@Singleton
class CartRepository @Inject constructor(
    private val cartDao: CartDao,
    private val apiService: ApiService,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {

    /**
     * Obtiene todos los items del carrito como un Flow reactivo.
     * Se actualiza automáticamente cuando cambian los datos locales.
     *
     * @return Flow con la lista de items del carrito
     */
    fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene el número total de items en el carrito como Flow reactivo.
     *
     * @return Flow con la cantidad total de productos en el carrito
     */
    fun getCartItemCount(): Flow<Int> {
        return cartDao.getAllCartItems().map { items ->
            items.sumOf { it.quantity }
        }
    }

    /**
     * Agrega un producto al carrito local, respetando la cantidad seleccionada.
     * Si ya existe el mismo producto y talla, suma la cantidad nueva a la existente.
     */
    suspend fun addToCart(
        productId: Int,
        productName: String,
        productPrice: Double,
        imageUrl: String?,
        sizeId: Int,
        sizeName: String,
        storeId: Int,
        storeName: String,
        quantity: Int = 1
    ) {
        val existing = cartDao.getCartItemByProductAndSize(productId, sizeId)
        if (existing != null) {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            cartDao.insertCartItem(
                CartItemEntity(
                    productId = productId,
                    productName = productName,
                    productPrice = productPrice,
                    productImageUrl = imageUrl,
                    sizeId = sizeId,
                    sizeName = sizeName,
                    quantity = quantity,
                    storeId = storeId,
                    storeName = storeName
                )
            )
        }
        updateWidget()
    }

    /**
     * Actualiza la cantidad de un item en el carrito.
     *
     * @param cartItem item del carrito con la nueva cantidad
     */
    suspend fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        val entity = CartItemEntity(
            id = cartItem.id,
            productId = cartItem.productId,
            productName = cartItem.productName,
            productPrice = cartItem.productPrice,
            productImageUrl = cartItem.productImageUrl,
            sizeId = cartItem.sizeId,
            sizeName = cartItem.sizeName,
            quantity = newQuantity,
            storeId = cartItem.storeId,
            storeName = cartItem.storeName
        )
        cartDao.updateCartItem(entity)
        updateWidget()
    }

    /**
     * Elimina un item específico del carrito.
     *
     * @param cartItem item a eliminar
     */
    suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(
            CartItemEntity(
                id = cartItem.id,
                productId = cartItem.productId,
                productName = cartItem.productName,
                productPrice = cartItem.productPrice,
                productImageUrl = cartItem.productImageUrl,
                sizeId = cartItem.sizeId,
                sizeName = cartItem.sizeName,
                quantity = cartItem.quantity,
                storeId = cartItem.storeId,
                storeName = cartItem.storeName
            )
        )
        updateWidget()
    }

    /**
     * Vacía el carrito eliminando todos los items.
     */
    suspend fun clearCart() {
        cartDao.clearCart()
        updateWidget()
    }

    /**
     * Crea una orden sincronizando primero el carrito local con Django.
     */
    suspend fun createOrder(
        storeId: Int,
        shippingAddress: String,
        shippingCity: String,
        shippingPhone: String
    ): Result<Order> {
        return try {
            val localItems = cartDao.getAllCartItems().first()

            // Primero vaciar el carrito de Django
            apiService.clearDjangoCart(storeId)

            // Sincronizar cada item local con Django
            for (item in localItems) {
                apiService.addCartItem(
                    CartItemAddDto(
                        storeId = item.storeId,
                        productId = item.productId,
                        sizeId = item.sizeId,
                        quantity = item.quantity
                    )
                )
            }

            // Crear la orden
            val response = apiService.createOrder(
                CreateOrderRequestDto(storeId, shippingAddress, shippingCity, shippingPhone)
            )
            if (response.isSuccessful) {
                val orderDto = response.body()!!
                val order = Order(
                    id = orderDto.id,
                    buyerName = orderDto.buyerName,
                    storeName = orderDto.storeName,
                    status = orderDto.status,
                    paymentStatus = orderDto.paymentStatus,
                    subtotal = orderDto.subtotal.toDoubleOrNull() ?: 0.0,
                    shippingCost = orderDto.shippingCost.toDoubleOrNull() ?: 0.0,
                    total = orderDto.total.toDoubleOrNull() ?: 0.0,
                    shippingAddress = orderDto.shippingAddress,
                    shippingCity = orderDto.shippingCity,
                    shippingPhone = orderDto.shippingPhone,
                    createdAt = orderDto.createdAt,
                    items = orderDto.items.map { item ->
                        OrderItem(
                            id = item.id,
                            productName = item.productName,
                            sizeName = item.sizeName,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice.toDoubleOrNull() ?: 0.0,
                            subtotal = item.subtotal.toDoubleOrNull() ?: 0.0
                        )
                    }
                )
                cartDao.clearCart()
                updateWidget() // Se actualiza aquí dado que clearCart() fue llamado internamente
                Result.success(order)
            } else {
                Result.failure(Exception("Error al crear la orden: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Limpia el carrito local sin afectar el carrito en Django.
     */
    suspend fun clearLocalCart() {
        cartDao.clearCart()
        updateWidget()
    }

    /**
     * Convierte una entidad de Room CartItemEntity a un modelo de dominio CartItem.
     */
    fun CartItemEntity.toDomain(): CartItem = CartItem(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = productImageUrl,
        sizeId = sizeId,
        sizeName = sizeName,
        quantity = quantity,
        storeId = storeId,
        storeName = storeName
    )

    /**
     * Solicita la actualización del widget del carrito en la pantalla de inicio.
     */
    private suspend fun updateWidget() {
        try {
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(com.fashionsaas.app.ui.widget.CartWidget::class.java)
            glanceIds.forEach { id ->
                com.fashionsaas.app.ui.widget.CartWidget().update(context, id)
            }
        } catch (e: Exception) {
            // El widget puede no estar agregado a la pantalla, se ignora el error
        }
    }
}