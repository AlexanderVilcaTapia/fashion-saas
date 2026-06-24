import { Link } from 'react-router-dom'

/**
 * Componente de pie de página de la aplicación.
 * Muestra enlaces de navegación secundarios e información del proyecto.
 */
const Footer = () => {
  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col md:flex-row justify-between items-center gap-4">

          {/* Logo */}
          <div>
            <span className="text-lg font-semibold text-gray-900">
              Fashion<span className="text-indigo-600">SaaS</span>
            </span>
            <p className="text-xs text-gray-400 mt-1">
              Plataforma SaaS para tiendas de ropa
            </p>
          </div>

          {/* Enlaces */}
          <div className="flex gap-6">
            <Link to="/" className="text-sm text-gray-500 hover:text-gray-900 transition-colors">
              Inicio
            </Link>
            <Link to="/login" className="text-sm text-gray-500 hover:text-gray-900 transition-colors">
              Iniciar sesión
            </Link>
            <Link to="/register" className="text-sm text-gray-500 hover:text-gray-900 transition-colors">
              Registrarse
            </Link>
          </div>

          {/* Copyright */}
          <p className="text-xs text-gray-400">
            © 2026 FashionSaaS. Todos los derechos reservados.
          </p>
        </div>
      </div>
    </footer>
  )
}

export default Footer