# SGA - Sistema de Gestión de Alojamiento · Siete Raíces

Proyecto de **Programación Avanzada** (Ingeniería de Sistemas y Computación, Universidad del Quindío) para *Siete Raíces – Apartamentos Turísticos* (Armenia, Quindío).

## Integrantes

- Jose Manuel Rodriguez Ospina
- Daniela Muñoz Arboleda
- Adrian David Navarro

## Estructura del repositorio

| Carpeta | Contenido |
|---|---|
| [backend/](backend/) | API REST con Spring Boot: dominio (DDD), casos de uso, persistencia, seguridad JWT, pruebas y documentación de las entregas ([backend/README.md](backend/README.md)) |
| [frontend/](frontend/) | Aplicación web con Vite, React, TypeScript y React Router ([frontend/README.md](frontend/README.md)) |
| [frontend/mockups/](frontend/mockups/) | Diseño de las pantallas, hecho antes de implementar el frontend |

## Ejecución

Backend (requiere JDK 25 y la clave JWT local descrita en [backend/README.md](backend/README.md)):

```bash
cd backend
./gradlew bootRun
```

Frontend (requiere Node.js), en otra terminal:

```bash
cd frontend
npm install
npm run dev
```

La API queda en `http://localhost:8080/api` y el frontend en `http://localhost:5173`.
