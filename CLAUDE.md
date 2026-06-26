# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-tenant fashion e-commerce SaaS platform with three separate services:
- **`backend-django/`** — Main REST API (Django 5 + DRF), port 8000
- **`backend-spring/`** — Store owner admin panel (Spring Boot 3 + Java 21), port 8080
- **`frontend-react/`** — Buyer-facing web app (React 19 + Vite + TailwindCSS 4), port 5173

## Development Commands

### Full stack (Docker)
```bash
docker compose up --build      # Start all services
docker compose down            # Stop all services
```

### Django backend
```bash
cd backend-django
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver
python manage.py createsuperuser
```

### Spring Boot backend
```bash
cd backend-spring
./mvnw spring-boot:run         # Linux/Mac
mvnw.cmd spring-boot:run       # Windows
./mvnw test                    # Run tests
```

### React frontend
```bash
cd frontend-react
npm install
npm run dev        # Dev server (port 5173)
npm run build      # Production build
npm run lint       # Oxlint
npm run preview    # Preview production build
```

## Architecture

### Authentication flow
JWT is used across all three services. Django issues tokens via `/api/auth/login/` (Simple JWT, 1h access / 7d refresh). Spring Boot validates tokens independently via its own JWT filter. The React `AuthContext` (`src/context/AuthContext.jsx`) holds auth state; the Axios instance in `src/services/api.js` attaches the Bearer token and automatically retries after a 401 by refreshing the token, or redirects to `/login` if refresh fails.

### Multi-tenancy
Each `STORE_OWNER` user has a OneToOne `Store`. Products, schedules, and inventory are scoped to a store. Buyers (`BUYER` role) browse stores by slug and place orders. The three roles — `BUYER`, `STORE_OWNER`, `ADMIN` — are on the custom Django `User` model (email-based, no username).

### API routing
React's Vite dev server proxies `/api/*` → `http://localhost:8000` (see `vite.config.js`). Django URL structure: `/api/auth/`, `/api/stores/`, `/api/products/`, `/api/orders/`. Spring Boot communicates with Django over HTTP and exposes its own dashboard/inventory/orders/sales endpoints on port 8080.

### React routing
Routes are defined in `src/App.jsx`. `PublicRoute` redirects authenticated users away from `/login` and `/register`. `PrivateRoute` redirects unauthenticated users to `/login`. Most page components under `src/pages/` are currently placeholders.

## Environment Setup

Copy and fill in `.env` from the example before running Django:
```
backend-django/.env.example
```
Key variables: `SECRET_KEY`, `DATABASE_URL` (defaults to SQLite in dev), `CLOUDINARY_*`, and Stripe keys used by Spring.

Spring Boot config is in `backend-spring/src/main/resources/application.properties` — set the DB URL, JWT secret, and Stripe keys there.

## Key Dependencies

| Service | Notable packages |
|---|---|
| Django | `djangorestframework`, `djangorestframework-simplejwt`, `cloudinary`, `psycopg2-binary` |
| Spring | `spring-boot-starter-security`, `jjwt`, `stripe-java` |
| React | `react-router-dom` v7, `axios`, `tailwindcss` v4 |
