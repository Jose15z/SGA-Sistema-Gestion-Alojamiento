# Frontend del SGA

Aplicación web del SGA con **Vite**, **React 19**, **TypeScript** y **React Router** (Guía 14).

## Ejecución

```bash
npm install
npm run dev
```

| Comando | Uso |
|---|---|
| `npm run dev` | Servidor de desarrollo en `http://localhost:5173` |
| `npm run build` | Verifica los tipos y genera la versión de producción en `dist/` |
| `npm run lint` | Revisa el código con oxlint |

## Estructura de `src/`

| Carpeta | Contenido |
|---|---|
| `paginas/` | Una página por ruta |
| `componentes/` | Piezas reutilizables entre páginas |
| `modelos/` | Tipos de TypeScript que reflejan los DTOs del backend |
| `datos/` | Datos de ejemplo mientras el frontend no consulta el backend |

## Diseño

Las pantallas se diseñaron primero como mockups y después se implementaron: ver [mockups/](mockups/).
