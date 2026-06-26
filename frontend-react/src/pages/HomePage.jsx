import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'

/**
 * Página principal de la plataforma.
 * Muestra las tiendas destacadas y un buscador de tiendas.
 */
const HomePage = () => {
  const [stores, setStores] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')

  /**
   * Carga las tiendas destacadas al montar el componente.
   */
  useEffect(() => {
    fetchFeaturedStores()
  }, [])

  /**
   * Obtiene las tiendas destacadas desde la API.
   */
  const fetchFeaturedStores = async () => {
    try {
      const response = await api.get('/stores/featured/')
      setStores(response.data)
    } catch {
      setStores([])
    } finally {
      setLoading(false)
    }
  }

  /**
   * Filtra las tiendas por nombre según el texto de búsqueda.
   */
  const filteredStores = stores.filter(store =>
    store.name.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">

      {/* Hero */}
      <div className="text-center mb-12">
        <h1 className="text-4xl font-semibold text-gray-900 mb-4">
          Descubre tiendas de ropa
        </h1>
        <p className="text-gray-500 text-lg mb-8">
          Explora las mejores tiendas y encuentra tu estilo
        </p>

        {/* Buscador */}
        <div className="max-w-md mx-auto">
          <input
            type="text"
            placeholder="Buscar tiendas..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full border border-gray-300 rounded-xl px-5 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
          />
        </div>
      </div>

      {/* Tiendas */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="bg-gray-100 rounded-xl h-48 animate-pulse" />
          ))}
        </div>
      ) : filteredStores.length === 0 ? (
        <div className="text-center py-20">
          <p className="text-gray-400 text-lg">No se encontraron tiendas.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredStores.map(store => (
            <Link
              key={store.id}
              to={`/stores/${store.slug}`}
              className="bg-white rounded-xl border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
            >
              {/* Banner */}
              <div className="h-32 bg-gradient-to-r from-indigo-50 to-purple-50 flex items-center justify-center">
                {store.logo ? (
                  <img
                    src={store.logo}
                    alt={store.name}
                    className="h-16 w-16 rounded-full object-cover border-2 border-white shadow"
                  />
                ) : (
                  <div className="h-16 w-16 rounded-full bg-indigo-100 flex items-center justify-center text-2xl font-semibold text-indigo-600">
                    {store.name.charAt(0)}
                  </div>
                )}
              </div>

              {/* Info */}
              <div className="p-5">
                <h2 className="font-semibold text-gray-900 text-base">{store.name}</h2>
                {store.city && (
                  <p className="text-xs text-gray-400 mt-1">{store.city}</p>
                )}
                {store.description && (
                  <p className="text-sm text-gray-500 mt-2 line-clamp-2">{store.description}</p>
                )}
                <div className="flex items-center justify-between mt-4">
                  <span className="text-xs text-gray-400">{store.total_products} productos</span>
                  <span className="text-xs text-indigo-600 font-medium">Ver tienda →</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

export default HomePage