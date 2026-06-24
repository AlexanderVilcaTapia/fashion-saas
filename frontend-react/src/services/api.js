import axios from 'axios'

/**
 * Instancia principal de Axios configurada con la URL base de la API.
 * Incluye interceptores para adjuntar el token JWT en cada petición
 * y manejar errores de autenticación automáticamente.
 */
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * Interceptor de peticiones.
 * Adjunta el token JWT del localStorage en el header Authorization.
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * Interceptor de respuestas.
 * Si el token expira (401), intenta refrescarlo automáticamente.
 * Si el refresco falla, redirige al login.
 */
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refresh_token')
        const response = await axios.post('/api/auth/refresh/', {
          refresh: refreshToken,
        })

        const newAccessToken = response.data.access
        localStorage.setItem('access_token', newAccessToken)
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`

        return api(originalRequest)
      } catch {
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export default api