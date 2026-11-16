# Frontend del SGA

Aplicación web del SGA con **Vite**, **React 19**, **TypeScript** y **React Router** (Guía 14), con los estilos del diseño de Figma ([mockups/](mockups/)).

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

| Carpeta o archivo | Contenido |
|---|---|
| `index.css` | Estilos globales del diseño: colores y fuentes en variables (`--green`, `--cream`, ...) y las clases de cada pantalla |
| `disenos/` | `DisenoAcceso` (login y registro) y `DisenoAplicacion` (barra lateral por rol, barra superior y contenido) |
| `paginas/` | Una página por ruta |
| `componentes/` | Piezas reutilizables: `Icono`, `Etiqueta`, `Marca`, `EncabezadoPagina`, `BarraLateral`, `Encabezado`, `PiePagina`, `TarjetaApartamento`, `PantallaPendiente` |
| `modelos/` | Tipos que reflejan los DTOs del backend (`apartamento`, `rol`) |
| `datos/` | Datos de ejemplo y el menú de cada rol (`navegacion.ts`) |
| `public/fotos/` | Fotos de los apartamentos usadas en el diseño |

Mientras no hay sesión (Guía 15), el rol se escoge en **Espacio de trabajo**, en la barra lateral.

## Rutas y reparto

| Ruta | Página | Mockup | Parte |
|---|---|---|---|
| `/login` | `Login` | 01 | Acceso y operación interna |
| `/registro` | `Registro` | 02 | Acceso y operación interna |
| `/apartamentos` | `Inicio` | 03 | Huésped |
| `/apartamentos/:identificacion` | `DetalleApartamento` | 04 | Huésped |
| `/reservas/nueva` | `NuevaReserva` | 04 | Huésped |
| `/mis-reservas` | `MisReservas` | 05 | Huésped |
| `/mis-reservas/:codigo` | `DetalleReserva` | 05 | Huésped |
| `/recepcion` | `Recepcion` (llegadas, salidas, check-in y check-out) | 06 | Acceso y operación interna |
| `/recepcion/nueva-reserva` | `NuevaReservaDirecta` | 07 | Acceso y operación interna |
| `/recepcion/reservas` | `ReservasYFolios` | 08 | Acceso y operación interna |
| `/recepcion/reservas/:codigo/folio` | `Folio` (pagos y cierre) | 08 | Acceso y operación interna |
| `/preparacion` | `Preparacion` | 09 | Por definir |
| `/administracion` | `Administracion` | 10–14 | Por definir |
| `/reportes` | `Reportes` | 15 | Por definir |

Las páginas que muestran **Pantalla en construcción** están listas para llenarse: cada una ya tiene su ruta, su título y el mockup de referencia.

## Convenciones (Guía 14)

- Nombres en español, con el lenguaje ubicuo del proyecto.
- Las páginas se exportan con `export default`; los componentes, con exportación nombrada.
- Las importaciones de tipos usan `import type`; React Router se importa desde `'react-router'`.
- Para los estilos se reutilizan las clases de `index.css` (por ejemplo `apartment-card`, `search-panel`, `booking-box`) antes de crear nuevas.
