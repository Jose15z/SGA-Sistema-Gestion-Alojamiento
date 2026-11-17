# SGA · Sistema de Gestión de Alojamiento

Aplicación web para **Siete Raíces – Apartamentos Turísticos**, en Armenia, Quindío. Centraliza la disponibilidad, las reservas y la operación diaria del alojamiento. Proyecto de Programación Avanzada de Ingeniería de Sistemas y Computación, Universidad del Quindío.

[Entrar a SGA](https://sga-sistema-gestion-alojamiento.vercel.app/login)

| Rol | Funciones disponibles |
| --- | --- |
| Huésped | Buscar apartamentos, consultar disponibilidad y tarifas, crear y gestionar sus reservas. |
| Recepción | Consultar llegadas y salidas, crear reservas directas y gestionar reservas y folios. |
| Personal de servicio | Consultar el estado de los apartamentos e iniciar o finalizar su preparación. |
| Administración | Gestionar usuarios internos y acceder a la operación diaria y la preparación. |

## Estructura

| Carpeta | Contenido |
| --- | --- |
| [backend/](backend/) | API REST en Spring Boot con seguridad JWT, persistencia y pruebas. |
| [frontend/](frontend/) | Aplicación React y TypeScript compilada con Vite. |
| [frontend/mockups/](frontend/mockups/) | Bocetos de referencia. Incluyen propuestas de pantallas que aún no están disponibles. |

## Ejecutar en local

Necesitas JDK 25, Node.js 22.12 o posterior y npm. Inicia el backend desde la raíz del repositorio:

```bash
cd backend
export SGA_JWT_SECRET="$(openssl rand -hex 48)"
./gradlew bootRun
```

En otra terminal, desde la raíz del repositorio:

```bash
cd frontend
npm ci
npm run dev
```

Abre [http://localhost:5173](http://localhost:5173). La API local escucha en `http://localhost:8080/api`. El perfil local carga los datos de demostración en H2 y los restablece al reiniciar. Las cuentas locales, Swagger y la configuración adicional están en el [README del backend](backend/README.md).

Para verificar cambios, ejecuta `./gradlew test` desde `backend/` y `npm run build` seguido de `npm run lint` desde `frontend/`.

## Despliegue

- **Vercel** publica el frontend desde `main`, con `frontend/` como directorio raíz. `VITE_API_URL` apunta a la [API de Render](https://sga-siete-raices-api.onrender.com/api/alojamiento) e incluye el sufijo `/api`.
- **Render** ejecuta el backend con Docker y vuelve a desplegar cuando cambian archivos de `backend/` en `main`. El perfil `render` utiliza `SGA_FRONTEND_ORIGIN` para autorizar el dominio del frontend.
- **Neon** aloja PostgreSQL. En Render, `SPRING_PROFILES_ACTIVE=render,neon` activa la conexión mediante `SGA_DB_JDBC_URL`, `SGA_DB_USER` y `SGA_DB_PASSWORD`. La clave JWT y la contraseña inicial de administración también se configuran en Render.

El primer acceso a la API puede tardar si el servicio de Render estaba inactivo.

## Integrantes

- Jose Manuel Rodriguez Ospina
- Daniela Muñoz Arboleda
- Adrian David Navarro
