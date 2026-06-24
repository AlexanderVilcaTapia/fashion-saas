import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

/**
 * Componente de navegación principal.
 * Muestra el menú de navegación con opciones según el estado de autenticación.
 * Incluye menú responsive para dispositivos móviles.
 */
const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  /**
   * Maneja el cierre de sesión del usuario.
   * Redirige al inicio después de cerrar sesión.
   */
  const handleLogout = async () => {
    await logout()
    navigate('/')
  }

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">

          {/* Logo */}
          <Link to="/" className="text-xl font-semibold text-gray-900 tracking-tight">
            Fashion<span className="text-indigo-600">SaaS</span>
          </Link>

          {/* Menu desktop */}
          <div className="hidden md:flex items-center gap-6">
            <Link to="/" className="text-sm text-gray-600 hover:text-gray-900 transition-colors">
              Inicio
            </Link>

            {isAuthenticated ? (
              <>
                <Link to="/orders" className="text-sm text-gray-600 hover:text-gray-900 transition-colors">
                  Mis Órdenes
                </Link>
                <Link to="/cart" className="text-sm text-gray-600 hover:text-gray-900 transition-colors">
                  Carrito
                </Link>
                <span className="text-sm text-gray-400">|</span>
                <span className="text-sm text-gray-600">{user?.first_name}</span>
                <button
                  onClick={handleLogout}
                  className="text-sm text-red-500 hover:text-red-700 transition-colors"
                >
                  Salir
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900 transition-colors">
                  Iniciar sesión
                </Link>
                <Link
                  to="/register"
                  className="text-sm bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors"
                >
                  Registrarse
                </Link>
              </>
            )}
          </div>

          {/* Botón menú móvil */}
          <button
            className="md:hidden text-gray-600 hover:text-gray-900"
            onClick={() => setMenuOpen(!menuOpen)}
          >
            {menuOpen ? '✕' : '☰'}
          </button>
        </div>

        {/* Menú móvil */}
        {menuOpen && (
          <div className="md:hidden pb-4 flex flex-col gap-3">
            <Link to="/" className="text-sm text-gray-600" onClick={() => setMenuOpen(false)}>
              Inicio
            </Link>
            {isAuthenticated ? (
              <>
                <Link to="/orders" className="text-sm text-gray-600" onClick={() => setMenuOpen(false)}>
                  Mis Órdenes
                </Link>
                <Link to="/cart" className="text-sm text-gray-600" onClick={() => setMenuOpen(false)}>
                  Carrito
                </Link>
                <button onClick={handleLogout} className="text-sm text-red-500 text-left">
                  Salir
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm text-gray-600" onClick={() => setMenuOpen(false)}>
                  Iniciar sesión
                </Link>
                <Link to="/register" className="text-sm text-indigo-600" onClick={() => setMenuOpen(false)}>
                  Registrarse
                </Link>
              </>
            )}
          </div>
        )}
      </div>
    </nav>
  )
}

export default Navbar