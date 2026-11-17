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
| `componentes/` | Piezas reutilizables: `Icono`, `Etiqueta`, `EtiquetaEstado`, `Marca`, `EncabezadoPagina`, `BarraLateral`, `Encabezado`, `PiePagina`, `Campo`, `Aviso` (confirmación breve), `DialogoConfirmacion`, `Cargando`, `MovimientosFolio`, `TarjetaApartamento`, `PantallaPendiente` |
| `modelos/` | Tipos que reflejan los DTOs del backend (`auth`, `error`, `rol`, `apartamento`, `reserva`, `folio`) |
| `esquemas/` | Esquemas de Zod de cada formulario (`login`, `usuario`, `recuperacion`, `reserva`, `pago`) |
| `servicios/` | Cliente HTTP único (`api.ts`), sesión (`sesion.ts`) y llamadas a la API por recurso (`authServicio`, `usuarioServicio`, `reservaServicio`, `folioServicio`, `apartamentoServicio`) |
| `utilidades/` | Traducción de errores de la API, fecha de hoy, formato de pesos y fechas, y ayudas de formularios |
| `datos/` | Datos de ejemplo, el menú de cada rol (`navegacion.ts`) y la foto de cada apartamento (`fotos.ts`) |
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

## Operación de la recepción

| Pantalla | Qué hace | API |
|---|---|---|
| Recepción del día | Indicadores, llegadas y salidas de un día (se puede escoger otro), confirmar, registrar llegada, no-show, registrar salida y reservas recientes | `GET /api/reservas/movimientos-del-dia`, `PUT /api/reservas/{codigo}/...` |
| Nueva reserva | Reserva por el canal DIRECTO con titular y acompañantes (`useFieldArray`), cotización en vivo y resumen con foto | `POST /api/cotizaciones`, `POST /api/reservas` |
| Reservas y folios | Listado con filtros (estado, canal, apartamento, documento del titular) y paginación; folio de la reserva seleccionada | `GET /api/reservas`, `GET /api/folios/{codigo}` |
| Folio | Movimientos, pagos (con sugerencia del anticipo del 30 % o del saldo), cerrar folio, confirmar, check-in, check-out, no-show y cancelación; ocupantes e historial | `POST /api/folios/{codigo}/pagos`, `PUT /api/folios/{codigo}/cerrar` |

Las reglas las decide el backend y la pantalla muestra su mensaje: sin el anticipo no se confirma, sin el folio cerrado no hay salida, con saldo solo el administrador cierra el folio (con motivo), el no-show se habilita desde las 20:00 del día de entrada y no se reservan noches ocupadas. Las acciones que no se pueden deshacer piden confirmación.

Los datos de ejemplo no tienen llegadas ni salidas **hoy**: en Recepción del día escoja otro día (por ejemplo, dentro de dos días sale RES-2025-00006) o cree una reserva directa para hoy y recorra todo el ciclo.

## Rutas y reparto

| Ruta | Página | Mockup | Parte |
|---|---|---|---|
| `/login` | `Login` | 01 | Acceso y operación interna · **lista** |
| `/registro` | `Registro` | 02 | Acceso y operación interna · **lista** |
| `/recuperar` | `RecuperarContrasena` | 01 | Acceso y operación interna · **lista** |
| `/apartamentos` | `Inicio` | 03 | Huésped |
| `/apartamentos/:identificacion` | `DetalleApartamento` | 04 | Huésped |
| `/reservas/nueva` | `NuevaReserva` | 04 | Huésped |
| `/mis-reservas` | `MisReservas` | 05 | Huésped |
| `/mis-reservas/:codigo` | `DetalleReserva` | 05 | Huésped |
| `/recepcion` | `Recepcion` (llegadas, salidas, check-in y check-out) | 06 | Acceso y operación interna · **lista** |
| `/recepcion/nueva-reserva` | `NuevaReservaDirecta` | 07 | Acceso y operación interna · **lista** |
| `/recepcion/reservas` | `ReservasYFolios` | 08 | Acceso y operación interna · **lista** |
| `/recepcion/reservas/:codigo/folio` | `Folio` (pagos y cierre) | 08 | Acceso y operación interna · **lista** |
| `/preparacion` | `Preparacion` | 09 | Por definir |
| `/administracion` | `Administracion` | 10–13 | Por definir |
| `/administracion/usuarios` | `UsuariosInternos` | 14 | Acceso y operación interna · **lista** |
| `/reportes` | `Reportes` | 15 | Por definir |

Las páginas que muestran **Pantalla en construcción** están listas para llenarse: cada una ya tiene su ruta, su título y el mockup de referencia. Para las del huésped sirven de ejemplo `NuevaReservaDirecta` (formulario con ocupantes y cotización) y `Folio` (consulta y acciones); los servicios `reservaServicio`, `folioServicio` y `apartamentoServicio` ya existen.

## Convenciones (Guía 14)

- Nombres en español, con el lenguaje ubicuo del proyecto.
- Las páginas se exportan con `export default`; los componentes, con exportación nombrada.
- Las importaciones de tipos usan `import type`; React Router se importa desde `'react-router'`.
- Para los estilos se reutilizan las clases de `index.css` (por ejemplo `apartment-card`, `search-panel`, `booking-box`) antes de crear nuevas.
