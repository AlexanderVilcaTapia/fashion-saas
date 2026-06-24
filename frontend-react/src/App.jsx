import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'

// Pages
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import StoreCatalogPage from './pages/StoreCatalogPage'
import ProductDetailPage from './pages/ProductDetailPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrderConfirmationPage from './pages/OrderConfirmationPage'
import OrderHistoryPage from './pages/OrderHistoryPage'
import NotFoundPage from './pages/NotFoundPage'

// Layout
import Navbar from './components/layout/Navbar'
import Footer from './components/layout/Footer'

/**
 * Componente de ruta privada.
 * Redirige al login si el usuario no está autenticado.
 *
 * @param {React.ReactNode} children componente a proteger
 */
const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <div className="flex justify-center items-center h-screen">Cargando...</div>
  return isAuthenticated ? children : <Navigate to="/login" />
}

/**
 * Componente de ruta pública.
 * Redirige al inicio si el usuario ya está autenticado.
 *
 * @param {React.ReactNode} children componente a mostrar
 */
const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <div className="flex justify-center items-center h-screen">Cargando...</div>
  return !isAuthenticated ? children : <Navigate to="/" />
}

/**
 * Componente principal de rutas de la aplicación.
 * Define la estructura de navegación pública y privada.
 */
const AppRoutes = () => {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Navbar />
      <main className="flex-1">
        <Routes>
          {/* Rutas públicas */}
          <Route path="/" element={<HomePage />} />
          <Route path="/stores/:slug" element={<StoreCatalogPage />} />
          <Route path="/stores/:storeSlug/products/:productSlug" element={<ProductDetailPage />} />
          <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
          <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />

          {/* Rutas privadas */}
          <Route path="/cart" element={<PrivateRoute><CartPage /></PrivateRoute>} />
          <Route path="/checkout" element={<PrivateRoute><CheckoutPage /></PrivateRoute>} />
          <Route path="/orders" element={<PrivateRoute><OrderHistoryPage /></PrivateRoute>} />
          <Route path="/orders/:orderId" element={<PrivateRoute><OrderConfirmationPage /></PrivateRoute>} />

          {/* 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}

/**
 * Componente raíz de la aplicación.
 * Envuelve todo con el proveedor de autenticación.
 */
const App = () => {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

export default App