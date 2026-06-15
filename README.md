\# Fashion SaaS Platform



Plataforma SaaS multitenant para tiendas de ropa. Las tiendas se registran y gestionan sus productos desde un panel administrativo. Los compradores acceden al catálogo desde la web o la app móvil.



\## Stack Tecnológico



| Área | Tecnología |

|------|------------|

| API Principal | Django 5 + Django REST Framework |

| Panel Admin | Spring Boot 3 + Java 21 |

| Web Comprador | React 18 + Vite + TailwindCSS |

| App Móvil | Kotlin + Jetpack Compose |

| Base de Datos | PostgreSQL |

| Autenticación | JWT + Firebase Auth |

| Almacenamiento | Cloudinary |

| Pagos | Stripe |

| IA | Google Gemini API |



\## Estructura del Repositorio



fashion-saas/



├── backend-django/     # API REST principal



├── backend-spring/     # Panel administrativo para tiendas



├── frontend-react/     # Web del comprador



├── mobile-android/     # App Android



└── docs/               # Documentación y diagramas



\## Arquitectura



Los compradores acceden a la plataforma desde React (web) o la app Android. Las tiendas gestionan su inventario y ventas desde el panel Spring Boot. Toda la lógica de negocio y datos pasa por la API Django.



\## Deploy



| Servicio | URL |

|----------|-----|

| API Django | Predicament |

| Panel Spring | Próximamente |

| Web React | Próximamente |



\## Equipo



\- Alexander Vilca Tapia

\- Camila Fuentes Zuniga

\- Adrian Flores Camma



