package com.fashionsaas.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fashionsaas.app.ui.auth.LoginScreen
import com.fashionsaas.app.ui.auth.RegisterScreen
import com.fashionsaas.app.ui.auth.AuthViewModel
import com.fashionsaas.app.ui.cart.CartScreen
import com.fashionsaas.app.ui.home.HomeScreen
import com.fashionsaas.app.ui.ai.AiRecommendationScreen
import com.fashionsaas.app.ui.maps.StoreMapScreen
import com.fashionsaas.app.ui.orders.OrderHistoryScreen
import com.fashionsaas.app.ui.product.ProductDetailScreen
import com.fashionsaas.app.ui.product.StoreCatalogScreen

/**
 * Objeto que define las rutas de navegación de la aplicación.
 */
object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val STORE_CATALOG = "store/{storeSlug}"
    const val PRODUCT_DETAIL = "store/{storeSlug}/product/{productSlug}"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val MAPS = "maps"
    const val AI_RECOMMENDATION = "ai_recommendation"

    fun storeCatalog(storeSlug: String) = "store/$storeSlug"
    fun productDetail(storeSlug: String, productSlug: String) = "store/$storeSlug/product/$productSlug"
}

/**
 * Grafo de navegación principal de Fashion SaaS.
 * Define todas las rutas y pantallas de la aplicación.
 */
@Composable
fun FashionNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            val cartViewModel: com.fashionsaas.app.ui.cart.CartViewModel = hiltViewModel()
            val cartItemCount by cartViewModel.cartItemCount.collectAsState(initial = 0)

            HomeScreen(
                onStoreClick = { storeSlug ->
                    navController.navigate(Routes.storeCatalog(storeSlug))
                },
                onLoginClick = { navController.navigate(Routes.LOGIN) },
                onCartClick = { navController.navigate(Routes.CART) },
                onMapsClick = { navController.navigate(Routes.MAPS) },
                onAiClick = { navController.navigate(Routes.AI_RECOMMENDATION) },
                onOrdersClick = { navController.navigate(Routes.ORDERS) },
                isAuthenticated = isAuthenticated,
                cartItemCount = cartItemCount
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }},
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }},
                onLoginClick = { navController.navigate(Routes.LOGIN) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.STORE_CATALOG,
            arguments = listOf(navArgument("storeSlug") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeSlug = backStackEntry.arguments?.getString("storeSlug") ?: ""
            StoreCatalogScreen(
                storeSlug = storeSlug,
                onProductClick = { productSlug ->
                    navController.navigate(Routes.productDetail(storeSlug, productSlug))
                },
                onBackClick = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(
                navArgument("storeSlug") { type = NavType.StringType },
                navArgument("productSlug") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val storeSlug = backStackEntry.arguments?.getString("storeSlug") ?: ""
            val productSlug = backStackEntry.arguments?.getString("productSlug") ?: ""
            ProductDetailScreen(
                storeSlug = storeSlug,
                productSlug = productSlug,
                onBackClick = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                onBackClick = { navController.popBackStack() },
                onCheckoutSuccess = { navController.navigate(Routes.ORDERS) },
                onLoginRequired = { navController.navigate(Routes.LOGIN) },
                onAiClick = { navController.navigate(Routes.AI_RECOMMENDATION) }
            )
        }

        composable(Routes.ORDERS) {
            OrderHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MAPS) {
            StoreMapScreen(
                onBackClick = { navController.popBackStack() },
                onStoreClick = { storeSlug ->
                    navController.navigate(Routes.storeCatalog(storeSlug))
                }
            )
        }

        composable(Routes.AI_RECOMMENDATION) {
            AiRecommendationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}