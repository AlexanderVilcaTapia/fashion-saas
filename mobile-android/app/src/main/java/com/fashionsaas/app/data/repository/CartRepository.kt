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
    private val apiService: ApiService
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
     * Obtiene el número de items en el carrito como un Flow reactivo.
     *
     * @return Flow con el conteo de items
     */
    fun getCartItemCount(): Flow<Int> {
        return cartDao.getCartItemCount()
    }

    /**
     * Agrega un producto al carrito local.
     * Si el producto ya existe con la misma talla, incrementa la cantidad.
     *
     * @param productId    identificador del producto
     * @param productName  nombre del producto
     * @param productPrice precio del producto
     * @param imageUrl     URL de la imagen del producto
     * @param sizeId       identificador de la talla
     * @param sizeName     nombre de la talla
     * @param storeId      identificador de la tienda
     * @param storeName    nombre de la tienda
     */
    suspend fun addToCart(
        productId: Int,
        productName: String,
        productPrice: Double,
        imageUrl: String?,
        sizeId: Int,
        sizeName: String,
        storeId: Int,
        storeName: String
    ) {
        val existing = cartDao.getCartItemByProductAndSize(productId, sizeId)
        if (existing != null) {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            cartDao.insertCartItem(
                CartItemEntity(
                    productId = productId,
                    productName = productName,
                    productPrice = productPrice,
                    productImageUrl = imageUrl,
                    sizeId = sizeId,
                    sizeName = sizeName,
                    quantity = 1,
                    storeId = storeId,
                    storeName = storeName
                )
            )
        }
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
    }

    /**
     * Vacía el carrito eliminando todos los items.
     */
    suspend fun clearCart() {
        cartDao.clearCart()
    }

    /**
     * Crea una orden sincronizando primero el carrito local con Django.
     *
     * @param storeId         identificador de la tienda
     * @param shippingAddress dirección de envío
     * @param shippingCity    ciudad de envío
     * @param shippingPhone   teléfono de contacto
     * @return Result con los datos de la orden creada
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
                Result.success(order)
            } else {
                Result.failure(Exception("Error al crear la orden: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
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