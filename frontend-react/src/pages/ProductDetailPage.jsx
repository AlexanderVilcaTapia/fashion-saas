import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

/**
 * Página de detalle de un producto.
 * Muestra imágenes, descripción, tallas disponibles y permite
 * agregar el producto al carrito.
 */
const ProductDetailPage = () => {
  const { storeSlug, productSlug } = useParams()
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [selectedSize, setSelectedSize] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [addingToCart, setAddingToCart] = useState(false)
  const [message, setMessage] = useState('')
  const [selectedImage, setSelectedImage] = useState(0)

  /**
   * Carga los datos del producto al montar el componente.
   */
  useEffect(() => {
    fetchProduct()
  }, [storeSlug, productSlug])

  /**
   * Obtiene los datos del producto desde la API.
   */
  const fetchProduct = async () => {
    try {
      const response = await api.get(`/products/${storeSlug}/${productSlug}/`)
      setProduct(response.data)
    } catch {
      setProduct(null)
    } finally {
      setLoading(false)
    }
  }

  /**
   * Agrega el producto al carrito del comprador.
   * Requiere que el usuario esté autenticado y haya seleccionado una talla.
   */
  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    if (!selectedSize) {
      setMessage('Por favor selecciona una talla.')
      return
    }
    setAddingToCart(true)
    try {
      await api.post('/orders/cart/items/', {
        store_id: product.store,
        product_id: product.id,
        size_id: selectedSize.id,
        quantity,
      })
      setMessage('Producto agregado al carrito.')
    } catch {
      setMessage('Error al agregar al carrito. Intenta nuevamente.')
    } finally {
      setAddingToCart(false)
    }
  }

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-gray-100 rounded-xl h-96 animate-pulse" />
          <div className="flex flex-col gap-4">
            <div className="bg-gray-100 rounded h-8 animate-pulse" />
            <div className="bg-gray-100 rounded h-6 w-1/3 animate-pulse" />
            <div className="bg-gray-100 rounded h-24 animate-pulse" />
          </div>
        </div>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-400 text-lg">Producto no encontrado.</p>
        <Link to="/" className="text-indigo-600 text-sm mt-4 inline-block">
          Volver al inicio
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm text-gray-400 mb-6">
        <Link to="/" className="hover:text-gray-600">Inicio</Link>
        <span>/</span>
        <Link to={`/stores/${storeSlug}`} className="hover:text-gray-600">{product.store_name}</Link>
        <span>/</span>
        <span className="text-gray-600">{product.name}</span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-10">

        {/* Imágenes */}
        <div className="flex flex-col gap-3">
          <div className="bg-gray-50 rounded-xl overflow-hidden h-96 flex items-center justify-center border border-gray-200">
            {product.images?.[selectedImage] ? (
              <img
                src={product.images[selectedImage].image}
                alt={product.name}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="text-gray-300 text-6xl">👗</div>
            )}
          </div>
          {product.images?.length > 1 && (
            <div className="flex gap-2">
              {product.images.map((img, i) => (
                <button
                  key={img.id}
                  onClick={() => setSelectedImage(i)}
                  className={`h-16 w-16 rounded-lg overflow-hidden border-2 transition-colors ${
                    selectedImage === i ? 'border-indigo-500' : 'border-gray-200'
                  }`}
                >
                  <img src={img.image} alt="" className="h-full w-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Info del producto */}
        <div className="flex flex-col gap-5">
          <div>
            <p className="text-sm text-gray-400 mb-1">{product.store_name}</p>
            <h1 className="text-2xl font-semibold text-gray-900">{product.name}</h1>
            {product.category_name && (
              <span className="text-xs text-indigo-600 bg-indigo-50 px-2 py-1 rounded-full mt-2 inline-block">
                {product.category_name}
              </span>
            )}
          </div>

          {/* Precio */}
          <div className="flex items-center gap-3">
            <span className="text-2xl font-semibold text-gray-900">
              S/. {product.final_price}
            </span>
            {product.has_discount && (
              <span className="text-base text-gray-400 line-through">
                S/. {product.price}
              </span>
            )}
          </div>

          {/* Descripción */}
          {product.description && (
            <p className="text-sm text-gray-500 leading-relaxed">{product.description}</p>
          )}

          {/* Tallas */}
          <div>
            <p className="text-sm font-medium text-gray-700 mb-2">Talla</p>
            <div className="flex gap-2 flex-wrap">
              {product.sizes?.map(size => (
                <button
                  key={size.id}
                  onClick={() => setSelectedSize(size)}
                  disabled={size.stock === 0}
                  className={`px-4 py-2 rounded-lg text-sm border transition-colors ${
                    selectedSize?.id === size.id
                      ? 'bg-indigo-600 text-white border-indigo-600'
                      : size.stock === 0
                      ? 'bg-gray-50 text-gray-300 border-gray-200 cursor-not-allowed'
                      : 'bg-white text-gray-700 border-gray-300 hover:border-indigo-400'
                  }`}
                >
                  {size.size}
                  {size.stock === 0 && ' (agotado)'}
                </button>
              ))}
            </div>
          </div>

          {/* Cantidad */}
          <div>
            <p className="text-sm font-medium text-gray-700 mb-2">Cantidad</p>
            <div className="flex items-center gap-3">
              <button
                onClick={() => setQuantity(q => Math.max(1, q - 1))}
                className="h-9 w-9 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors"
              >
                -
              </button>
              <span className="text-sm font-medium w-8 text-center">{quantity}</span>
              <button
                onClick={() => setQuantity(q => Math.min(selectedSize?.stock || 10, q + 1))}
                className="h-9 w-9 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors"
              >
                +
              </button>
            </div>
          </div>

          {/* Mensaje */}
          {message && (
            <div className={`text-sm px-4 py-3 rounded-lg ${
              message.includes('Error') || message.includes('selecciona')
                ? 'bg-red-50 text-red-600 border border-red-200'
                : 'bg-green-50 text-green-600 border border-green-200'
            }`}>
              {message}
            </div>
          )}

          {/* Botón agregar al carrito */}
          <button
            onClick={handleAddToCart}
            disabled={addingToCart}
            className="bg-indigo-600 text-white py-3 rounded-xl text-sm font-medium hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {addingToCart ? 'Agregando...' : 'Agregar al carrito'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ProductDetailPage