# Matriz completa de acceso de la Guía 12

Prefijo `/api`. Política aplicada en `SeguridadConfig`; reservas y folios agregan autorización por método y titular. Las rutas nuevas se deniegan hasta asignarles acceso explícito.

| Método | Endpoint | Acceso |
|---|---|---|
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/recuperacion` | Público |
| POST | `/api/auth/recuperacion/restablecer` | Público |
| GET | `/api/usuarios/yo` | cualquier usuario autenticado |
| PUT | `/api/usuarios/yo` | cualquier usuario autenticado |
| PUT | `/api/usuarios/yo/contrasena` | cualquier usuario autenticado |
| GET | `/api/usuarios` | ADMINISTRADOR |
| POST | `/api/usuarios` | ADMINISTRADOR |
| PUT | `/api/usuarios/{identificacion}/rol` | ADMINISTRADOR |
| DELETE | `/api/usuarios/{identificacion}` | ADMINISTRADOR |
| GET | `/api/alojamiento` | Público |
| PUT | `/api/alojamiento` | ADMINISTRADOR |
| POST | `/api/apartamentos` | ADMINISTRADOR |
| GET | `/api/apartamentos` | ADMINISTRADOR, RECEPCIONISTA, PERSONAL_SERVICIO |
| GET | `/api/apartamentos/disponibles` | Público |
| GET | `/api/apartamentos/{identificacion}` | Público |
| PUT | `/api/apartamentos/{identificacion}` | ADMINISTRADOR |
| GET | `/api/apartamentos/{identificacion}/calendario` | Público |
| GET | `/api/apartamentos/{identificacion}/tarifas/historial` | ADMINISTRADOR, RECEPCIONISTA |
| POST | `/api/apartamentos/{identificacion}/novedades` | ADMINISTRADOR, RECEPCIONISTA, PERSONAL_SERVICIO |
| GET | `/api/apartamentos/{identificacion}/novedades` | ADMINISTRADOR, RECEPCIONISTA, PERSONAL_SERVICIO |
| PUT | `/api/apartamentos/{identificacion}/capacidad` | ADMINISTRADOR |
| PUT | `/api/apartamentos/{identificacion}/tarifas` | ADMINISTRADOR |
| PUT | `/api/apartamentos/{identificacion}/retirar` | ADMINISTRADOR |
| PUT | `/api/apartamentos/{identificacion}/reactivar` | ADMINISTRADOR |
| PUT | `/api/apartamentos/{identificacion}/iniciar-preparacion` | ADMINISTRADOR, RECEPCIONISTA, PERSONAL_SERVICIO |
| PUT | `/api/apartamentos/{identificacion}/marcar-preparado` | ADMINISTRADOR, RECEPCIONISTA, PERSONAL_SERVICIO |
| PUT | `/api/apartamentos/{identificacion}/fuera-de-servicio` | ADMINISTRADOR |
| PUT | `/api/apartamentos/{identificacion}/reincorporar` | ADMINISTRADOR |
| POST | `/api/apartamentos/{identificacion}/bloqueos` | ADMINISTRADOR |
| GET | `/api/apartamentos/{identificacion}/bloqueos` | ADMINISTRADOR, RECEPCIONISTA |
| PUT | `/api/bloqueos/{identificacion}/levantar` | ADMINISTRADOR |
| POST | `/api/temporadas` | ADMINISTRADOR |
| GET | `/api/temporadas` | ADMINISTRADOR, RECEPCIONISTA |
| POST | `/api/politicas-cancelacion` | ADMINISTRADOR |
| GET | `/api/politicas-cancelacion/vigente` | Público |
| POST | `/api/cotizaciones` | Público |
| POST | `/api/reservas` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| GET | `/api/reservas` | ADMINISTRADOR, RECEPCIONISTA |
| GET | `/api/reservas/movimientos-del-dia` | ADMINISTRADOR, RECEPCIONISTA |
| GET | `/api/reservas/{codigo}` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| PUT | `/api/reservas/{codigo}/confirmar` | ADMINISTRADOR, RECEPCIONISTA |
| PUT | `/api/reservas/{codigo}/cancelar` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| PUT | `/api/reservas/{codigo}/hora-llegada` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| PUT | `/api/reservas/{codigo}/novedad-llegada` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| PUT | `/api/reservas/{codigo}/resolver-novedad` | ADMINISTRADOR, RECEPCIONISTA |
| PUT | `/api/reservas/{codigo}/modificacion` | ADMINISTRADOR, RECEPCIONISTA |
| POST | `/api/reservas/{codigo}/vehiculos` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| PUT | `/api/reservas/{codigo}/registrar-llegada` | ADMINISTRADOR, RECEPCIONISTA |
| PUT | `/api/reservas/{codigo}/registrar-salida` | ADMINISTRADOR, RECEPCIONISTA |
| PUT | `/api/reservas/{codigo}/no-show` | ADMINISTRADOR, RECEPCIONISTA |
| GET | `/api/folios/{codigoReserva}` | ADMINISTRADOR, RECEPCIONISTA, HUESPED (solo sobre sus reservas) |
| POST | `/api/folios/{codigoReserva}/pagos` | ADMINISTRADOR, RECEPCIONISTA |
| POST | `/api/folios/{codigoReserva}/cargos` | ADMINISTRADOR, RECEPCIONISTA |
| POST | `/api/folios/{codigoReserva}/servicios` | ADMINISTRADOR, RECEPCIONISTA |
| PUT | `/api/folios/{codigoReserva}/cerrar` | ADMINISTRADOR, RECEPCIONISTA |

## Condiciones adicionales

- El huésped crea por PORTAL y con el documento del titular igual al del JWT.
- Recepción crea por DIRECTO. Administración conserva DIRECTO y EXTERNO para la operación existente.
- En reservas y folios, un huésped recibe 403 tanto para un recurso ajeno como para un código válido inexistente. Recepción y administración reciben 404 si no existe.
- Los códigos deben cumplir RES-YYYY-NNNNN; RES-NO-EXISTE no cumple el formato y se reemplaza por RES-2026-99999 en las pruebas.
- El calendario público muestra disponibilidad; solo administración y recepción ven códigos de reserva. Huéspedes, anónimos y servicio no los ven.
- Los roles internos solo los asigna administración; el registro público ignora cualquier rol solicitado y crea HUESPED.
- Los cierres de folio con saldo requieren autorización de administración en el caso de uso.
- Swagger, OpenAPI y H2 están disponibles solo con sga.seguridad.herramientas-desarrollo=true. En despliegue usar false y deshabilitar también spring.h2.console.enabled.

Todos los endpoints del contrato ya existen, incluidos tarifas, temporadas, bloqueos y preparación operativa; no fue necesario crear rutas ficticias. Se conserva el catálogo público del SGA (alojamiento, disponibilidad, detalle de apartamentos, cotización y política vigente), sin exponer información de titulares.
