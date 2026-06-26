import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../services/api'

/**
 * Página de catálogo de una tienda específica.
 * Muestra los productos de la tienda con filtros por categoría y búsqueda.
 */
const StoreCatalogPage = () => {
  const { slug } = useParams()
  const [store, setStore] = useState(null)
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')

  /**
   * Carga los datos de la tienda y sus productos al montar el componente
   * o cuando cambia el slug o los filtros.
   */
  useEffect(() => {
    fetchStoreData()
  }, [slug, selectedCategory, search])

  /**
   * Obtiene los datos de la tienda y sus productos desde la API.
   */
  const fetchStoreData = async () => {
    try {
      setLoading(true)
      const [storeRes, productsRes, categoriesRes] = await Promise.all([
        api.get(`/stores/${slug}/`),
        api.get(`/stores/${slug}/products/`, {
          params: { category: selectedCategory || undefined, search: search || undefined }
        }),
        api.get('/products/categories/')
      ])
      setStore(storeRes.data)
      setProducts(productsRes.data)
      setCategories(categoriesRes.data)
    } catch {
      setStore(null)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12">
        <div className="h-40 bg-gray-100 rounded-xl animate-pulse mb-8" />
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="bg-gray-100 rounded-xl h-64 animate-pulse" />
          ))}
        </div>
      </div>
    )
  }

  if (!store) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-400 text-lg">Tienda no encontrada.</p>
        <Link to="/" className="text-indigo-600 text-sm mt-4 inline-block">
          Volver al inicio
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

      {/* Header de la tienda */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 mb-8 flex items-center gap-5">
        {store.logo ? (
          <img
            src={store.logo}
            alt={store.name}
            className="h-16 w-16 rounded-full object-cover border border-gray-200"
          />
        ) : (
          <div className="h-16 w-16 rounded-full bg-indigo-100 flex items-center justify-center text-2xl font-semibold text-indigo-600">
            {store.name.charAt(0)}
          </div>
        )}
        <div>
          <h1 className="text-xl font-semibold text-gray-900">{store.name}</h1>
          {store.city && <p className="text-sm text-gray-400">{store.city}</p>}
          {store.description && <p className="text-sm text-gray-500 mt-1">{store.description}</p>}
        </div>
      </div>

      {/* Filtros */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <input
          type="text"
          placeholder="Buscar productos..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="flex-1 border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="">Todas las categorías</option>
          {categories.map(cat => (
            <option key={cat.id} value={cat.slug}>{cat.name}</option>
          ))}
        </select>
      </div>

      {/* Grid de productos */}
      {products.length === 0 ? (
        <div className="text-center py-20">
          <p className="text-gray-400">No se encontraron productos.</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {products.map(product => (
            <Link
              key={product.id}
              to={`/stores/${slug}/products/${product.slug}`}
              className="bg-white rounded-xl border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
            >
              {/* Imagen */}
              <div className="h-48 bg-gray-50 flex items-center justify-center">
                {product.images?.[0] ? (
                  <img
                    src={product.images[0].image}
                    alt={product.name}
                    className="h-full w-full object-cover"
                    loading="lazy"
                  />
                ) : (
                  <div className="text-gray-300 text-4xl">👗</div>
                )}
              </div>

              {/* Info */}
              <div className="p-4">
                <h3 className="text-sm font-medium text-gray-900 line-clamp-1">{product.name}</h3>
                <div className="flex items-center gap-2 mt-1">
                  {product.has_discount ? (
                    <>
                      <span className="text-sm font-semibold text-indigo-600">
                        S/. {product.final_price}
                      </span>
                      <span className="text-xs text-gray-400 line-through">
                        S/. {product.price}
                      </span>
                    </>
                  ) : (
                    <span className="text-sm font-semibold text-gray-900">
                      S/. {product.price}
                    </span>
                  )}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

export default StoreCatalogPage