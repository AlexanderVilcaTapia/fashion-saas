package com.fashionsaas.app.data.remote.api

import com.fashionsaas.app.data.remote.dto.CreateOrderRequestDto
import com.fashionsaas.app.data.remote.dto.LoginRequestDto
import com.fashionsaas.app.data.remote.dto.LoginResponseDto
import com.fashionsaas.app.data.remote.dto.OrderDto
import com.fashionsaas.app.data.remote.dto.ProductDto
import com.fashionsaas.app.data.remote.dto.ProductListDto
import com.fashionsaas.app.data.remote.dto.RegisterRequestDto
import com.fashionsaas.app.data.remote.dto.RegisterResponseDto
import com.fashionsaas.app.data.remote.dto.StoreDto
import com.fashionsaas.app.data.remote.dto.UserDto
import com.fashionsaas.app.data.remote.dto.CartItemAddDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para consumir la API REST de Django.
 * Define todos los endpoints disponibles en la aplicación.
 */
interface ApiService {

    // ===========================
    // Autenticación
    // ===========================

    /**
     * Inicia sesión con email y contraseña.
     *
     * @param request credenciales del usuario
     * @return tokens JWT de acceso y refresco
     */
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    /**
     * Registra un nuevo usuario comprador.
     *
     * @param request datos del nuevo usuario
     * @return datos del usuario creado y tokens JWT
     */
    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequestDto): Response<RegisterResponseDto>

    /**
     * Obtiene los datos del usuario autenticado.
     *
     * @return datos del usuario autenticado
     */
    @GET("auth/me/")
    suspend fun getCurrentUser(): Response<UserDto>

    // ===========================
    // Tiendas
    // ===========================

    /**
     * Obtiene las tiendas destacadas para la pantalla principal.
     *
     * @return lista de tiendas activas destacadas
     */
    @GET("stores/featured/")
    suspend fun getFeaturedStores(): Response<List<StoreDto>>

    /**
     * Obtiene la lista de tiendas activas con búsqueda opcional.
     *
     * @param search texto de búsqueda opcional
     * @param city   filtro por ciudad opcional
     * @return lista de tiendas activas
     */
    @GET("stores/")
    suspend fun getStores(
        @Query("search") search: String? = null,
        @Query("city") city: String? = null
    ): Response<List<StoreDto>>

    /**
     * Obtiene el detalle de una tienda por su slug.
     *
     * @param slug identificador único de la tienda
     * @return datos de la tienda
     */
    @GET("stores/{slug}/")
    suspend fun getStoreBySlug(@Path("slug") slug: String): Response<StoreDto>

    // ===========================
    // Productos
    // ===========================

    /**
     * Obtiene los productos destacados para la pantalla principal.
     *
     * @return lista de productos destacados
     */
    @GET("products/featured/")
    suspend fun getFeaturedProducts(): Response<List<ProductDto>>

    /**
     * Obtiene los productos de una tienda específica.
     *
     * @param storeSlug slug de la tienda
     * @param category  filtro por categoría opcional
     * @param search    texto de búsqueda opcional
     * @return lista de productos de la tienda
     */
    @GET("stores/{slug}/products/")
    suspend fun getStoreProducts(
        @Path("slug") storeSlug: String,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<ProductDto>>

    /**
     * Obtiene el detalle de un producto específico.
     *
     * @param storeSlug   slug de la tienda
     * @param productSlug slug del producto
     * @return datos del producto
     */
    @GET("products/{store_slug}/{product_slug}/")
    suspend fun getProductDetail(
        @Path("store_slug") storeSlug: String,
        @Path("product_slug") productSlug: String
    ): Response<ProductDto>

    /**
     * Busca productos en el catálogo general.
     *
     * @param search   texto de búsqueda
     * @param category filtro por categoría opcional
     * @return lista paginada de productos
     */
    @GET("products/")
    suspend fun searchProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): Response<ProductListDto>

    // ===========================
    // Órdenes
    // ===========================

    /**
     * Crea una nueva orden a partir del carrito del usuario.
     *
     * @param request datos de envío de la orden
     * @return datos de la orden creada
     */
    @POST("orders/create/")
    suspend fun createOrder(@Body request: CreateOrderRequestDto): Response<OrderDto>

    /**
     * Obtiene el historial de órdenes del usuario autenticado.
     *
     * @return lista de órdenes del usuario
     */
    @GET("orders/")
    suspend fun getOrders(): Response<List<OrderDto>>

    /**
     * Agrega un item al carrito en Django.
     *
     * @param item datos del item a agregar
     * @return respuesta del carrito actualizado
     */
    @POST("orders/cart/items/")
    suspend fun addCartItem(@Body item: CartItemAddDto): Response<Any>

    /**
     * Vacía el carrito en Django.
     *
     * @param storeId identificador de la tienda
     * @return respuesta de confirmación
     */
    @DELETE("orders/cart/")
    suspend fun clearDjangoCart(@Query("store_id") storeId: Int): Response<Any>
}