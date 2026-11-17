# Frontend del SGA

Aplicación web del SGA con **Vite**, **React 19**, **TypeScript** y **React Router** (Guía 14), formularios con **React Hook Form** y **Zod**, y la API consumida con **Axios** (Guía 15). Los estilos siguen el diseño de Figma ([mockups/](mockups/)).

## Ejecución

El backend debe estar corriendo en `http://localhost:8080` (ver [backend/README.md](../backend/README.md)); ya autoriza por CORS a `http://localhost:5173`.

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
| `modelos/` | Tipos que reflejan los DTOs del backend (`auth`, `error`, `rol`, `apartamento`) |
| `esquemas/` | Esquemas de Zod de cada formulario (`login`, `usuario`, `recuperacion`) |
| `servicios/` | Cliente HTTP único (`api.ts`), sesión (`sesion.ts`) y llamadas a la API por recurso |
| `utilidades/` | Traducción de errores de la API, fecha de hoy y ayudas de formularios |
| `datos/` | Datos de ejemplo y el menú de cada rol (`navegacion.ts`) |
| `public/fotos/` | Fotos de los apartamentos usadas en el diseño |

## Acceso y roles

| Regla | Dónde se cumple |
|---|---|
| El rol lo define la cuenta con la que se ingresa: viene en el token y arma el menú | `Login`, `DisenoAplicacion` |
| El registro público siempre crea un **Huésped**; el formulario no tiene campo de rol | `Registro` y backend (`Usuario.registrarHuesped`) |
| Solo el **Administrador** crea usuarios con otro rol (`/administracion/usuarios`) | `UsuariosInternos` y backend (`/api/usuarios` solo para ADMINISTRADOR) |
| Sin sesión o con el token vencido (30 min), las pantallas internas llevan al login | `DisenoAplicacion` |
| Cada rol solo entra a sus pantallas; si escribe otra dirección vuelve a su inicio | `datos/navegacion.ts` (misma matriz que `SeguridadConfig`) |
| Contraseña de mínimo 8 caracteres con mayúscula y número; correo y documento únicos | Esquemas de Zod y backend (400 / 409) |

"Recordarme" guarda la sesión en `localStorage` (sobrevive al cerrar el navegador); sin marcarlo, en `sessionStorage`. Como explica la Guía 15, `localStorage` es legible por cualquier script de la página: el riesgo se reconoce y se mitigaría con una cookie `HttpOnly`.

Usuarios de prueba: `admin@sieteraices.co` / `Admin123*`, `recepcion@sieteraices.co` / `Recepcion123*`, `servicio@sieteraices.co` / `Servicio123*`, `ana.ramirez@correo.com` / `Huesped123*`. En la recuperación de contraseña el código de 6 dígitos aparece en la consola del backend.

## Rutas y reparto

| Ruta | Página | Mockup | Parte |
|---|---|---|---|
| `/login` | `Login` | 01 | Acceso y operación interna |
| `/registro` | `Registro` | 02 | Acceso y operación interna |
| `/recuperar` | `RecuperarContrasena` | 01 | Acceso y operación interna |
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
| `/administracion` | `Administracion` | 10–13 | Por definir |
| `/administracion/usuarios` | `UsuariosInternos` | 14 | Acceso y operación interna |
| `/reportes` | `Reportes` | 15 | Por definir |

Las páginas que muestran **Pantalla en construcción** están listas para llenarse: cada una ya tiene su ruta, su título y el mockup de referencia.

## Convenciones (Guía 14)

- Nombres en español, con el lenguaje ubicuo del proyecto.
- Las páginas se exportan con `export default`; los componentes, con exportación nombrada.
- Las importaciones de tipos usan `import type`; React Router se importa desde `'react-router'`.
- Para los estilos se reutilizan las clases de `index.css` (por ejemplo `apartment-card`, `search-panel`, `booking-box`) antes de crear nuevas.
