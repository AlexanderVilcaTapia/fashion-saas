import { createContext, useContext, useState, useEffect } from 'react'
import api from '../services/api'

/**
 * Contexto de autenticación global.
 * Provee el estado del usuario y las funciones de login/logout
 * a todos los componentes de la aplicación.
 */
const AuthContext = createContext(null)

/**
 * Proveedor del contexto de autenticación.
 * Maneja el estado del usuario, tokens JWT y persistencia en localStorage.
 *
 * @param {React.ReactNode} children componentes hijos
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  /**
   * Al montar el componente, verifica si hay un token guardado
   * y carga los datos del usuario autenticado.
   */
  useEffect(() => {
    const token = localStorage.getItem('access_token')
    if (token) {
      fetchCurrentUser()
    } else {
      setLoading(false)
    }
  }, [])

  /**
   * Obtiene los datos del usuario autenticado desde la API.
   */
  const fetchCurrentUser = async () => {
    try {
      const response = await api.get('/auth/me/')
      setUser(response.data)
    } catch {
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    } finally {
      setLoading(false)
    }
  }

  /**
   * Inicia sesión con email y contraseña.
   * Guarda los tokens en localStorage y carga los datos del usuario.
   *
   * @param {string} email    correo del usuario
   * @param {string} password contraseña del usuario
   */
  const login = async (email, password) => {
    const response = await api.post('/auth/login/', { email, password })
    const { access, refresh } = response.data
    localStorage.setItem('access_token', access)
    localStorage.setItem('refresh_token', refresh)
    await fetchCurrentUser()
    return response.data
  }

  /**
   * Cierra la sesión del usuario.
   * Elimina los tokens del localStorage y limpia el estado.
   */
  const logout = async () => {
    try {
      const refresh = localStorage.getItem('refresh_token')
      await api.post('/auth/logout/', { refresh })
    } catch {
      // Si falla el logout en el servidor, igual limpiamos localmente
    } finally {
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      setUser(null)
    }
  }

  /**
   * Registra un nuevo usuario en la plataforma.
   *
   * @param {object} userData datos del nuevo usuario
   */
  const register = async (userData) => {
    const response = await api.post('/auth/register/', userData)
    const { tokens } = response.data
    localStorage.setItem('access_token', tokens.access)
    localStorage.setItem('refresh_token', tokens.refresh)
    await fetchCurrentUser()
    return response.data
  }

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      login,
      logout,
      register,
      isAuthenticated: !!user,
      isStoreOwner: user?.role === 'store_owner',
      isBuyer: user?.role === 'buyer',
    }}>
      {children}
    </AuthContext.Provider>
  )
}

/**
 * Hook personalizado para acceder al contexto de autenticación.
 *
 * @returns {object} contexto de autenticación
 */
export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}

export default AuthContext