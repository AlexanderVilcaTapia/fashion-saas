# Fashion SaaS Platform

Plataforma SaaS multitenant para tiendas de ropa. Las tiendas se registran y gestionan sus productos desde un panel administrativo. Los compradores acceden al catálogo desde la web o la app móvil.

## Stack Tecnológico

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
| IA | Google Gemini API (gemini-2.0-flash) |

## Componente de IA

La app Android integra Gemini AI como stylist personal. Analiza los productos del carrito del usuario y genera recomendaciones de outfits personalizadas con combinaciones, accesorios y ocasiones de uso. La IA está integrada en el flujo central de la app — no es un botón adicional sino parte del proceso de compra.

## Arquitectura

Los compradores acceden a la plataforma desde React (web) o la app Android. Las tiendas gestionan su inventario y ventas desde el panel Spring Boot. Toda la lógica de negocio y datos pasa por la API Django.

## Deploy

| Servicio | URL |
|----------|-----|
| API Django | https://fashion-saas-production.up.railway.app |
| Panel Spring | https://fashion-saas-admin.onrender.com |
| Web React | Próximamente |

## Estructura del Repositorio

# Fashion SaaS Platform

Plataforma SaaS multitenant para tiendas de ropa. Las tiendas se registran y gestionan sus productos desde un panel administrativo. Los compradores acceden al catálogo desde la web o la app móvil.

## Stack Tecnológico

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
| IA | Google Gemini API (gemini-2.0-flash) |

## Componente de IA

La app Android integra Gemini AI como stylist personal. Analiza los productos del carrito del usuario y genera recomendaciones de outfits personalizadas con combinaciones, accesorios y ocasiones de uso. La IA está integrada en el flujo central de la app — no es un botón adicional sino parte del proceso de compra.

## Arquitectura

Los compradores acceden a la plataforma desde React (web) o la app Android. Las tiendas gestionan su inventario y ventas desde el panel Spring Boot. Toda la lógica de negocio y datos pasa por la API Django.

## Deploy

| Servicio | URL |
|----------|-----|
| API Django | https://fashion-saas-production.up.railway.app |
| Panel Spring | https://fashion-saas-admin.onrender.com |
| Web React | Próximamente |

## Estructura del Repositorio

fashion-saas/
├── backend-django/     # API REST principal
├── backend-spring/     # Panel administrativo para tiendas
├── frontend-react/     # Web del comprador
└── mobile-android/     # App Android del comprador

## Configuración del Proyecto Android

### API Keys requeridas

Crea el archivo `mobile-android/local.properties` con las siguientes claves:

```properties
sdk.dir=TU_RUTA_SDK_ANDROID
DJANGO_BASE_URL=https://fashion-saas-production.up.railway.app
GEMINI_API_KEY=TU_GEMINI_API_KEY
MAPS_API_KEY=TU_MAPS_API_KEY
```

### Firebase

1. Crea un proyecto en Firebase Console
2. Agrega una app Android con package `com.fashionsaas.app`
3. Descarga `google-services.json` y colócalo en `mobile-android/app/`

### Obtener API Keys

- **Gemini API:** https://aistudio.google.com
- **Maps API:** https://console.cloud.google.com → Maps SDK for Android
- **Firebase:** https://console.firebase.google.com

### Ejecutar el proyecto

```bash
cd mobile-android
.\gradlew installDebug
```

## Equipo

- Alexander Vilca Tapia
- Camila Fuentes Zuniga

## Capturas de la App

*Próximamente*

## Video Demo

*Próximamente*