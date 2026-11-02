# Entrega 2 — Backend del SGA · Siete Raíces

Este documento describe lo implementado en la Entrega 2 y cómo responde a la retroalimentación de la sustentación de la Entrega 1. El contrato de la API es [`docs/api/openapi.yaml`](api/openapi.yaml); la colección de pruebas es [`docs/pruebas/SGA-Entrega2.postman_collection.json`](pruebas/SGA-Entrega2.postman_collection.json); el diagrama de clases actualizado está en [`docs/diagramas/DiagramaDeClases.md`](diagramas/DiagramaDeClases.md).

## 1. Correcciones acordadas en la sustentación de la Entrega 1

| Observación del profesor | Corrección aplicada |
|---|---|
| El no-show aparece a las 19:00 y a las 20:00 en distintos documentos | El valor oficial es **20:00 del día de entrada**. Vive en un solo lugar (`sga.alojamiento.hora-limite-no-show=20:00`), lo usa `Reserva.declararNoShow` (L-15) y lo prueban `ParametrosDePrueba`, `ReservasUseCasesTest` y la colección (carpeta 6). El texto "7:00 p. m." de la sección *Política de no-show* del Anexo A queda reemplazado por 20:00. |
| Consolidar al menos tres invariantes por raíz y distinguir qué protege el agregado y qué requiere coordinación | Sección 2. Cada raíz tiene además su lista en el Javadoc de la clase. |
| Completar la correspondencia de los casos de uso adicionales del catálogo | Sección 4: los casos de uso del catálogo (cotizar, modificar, recibir reserva externa, solicitar vehículo, crear bloqueo, retirar apartamento) están implementados y expuestos. |
| Aplicar identidad, reemplazo y ciclo de vida a conceptos concretos | Sección 3. |
| Desarrollar razones y consecuencias de las reglas propias | Sección 5. |
| Presentar una situación que siga el recorrido completo hasta el endpoint | Sección 8. |

### Alineación con el documento del proyecto (Proyecto-Final-SGA)

Después de una revisión contra el enunciado se cerraron estas brechas:

| Sección del enunciado | Qué se ajustó |
|---|---|
| 3.6 El dominio no conoce al usuario | `Usuario`, `Rol`, `CorreoElectronico` y su puerto pasaron a `application.seguridad` / `application.port`. El dominio solo conoce al **titular** por su documento (`Reserva.perteneceA`). |
| 7.1 Roles | Se agregó **PERSONAL_SERVICIO**: consulta apartamentos por estado operativo, inicia y termina la preparación y registra novedades; no declara FUERA_DE_SERVICIO, no bloquea y no ve reservas. |
| 7.2 Usuarios | Registro con teléfono y fecha de nacimiento, contraseña segura (8+, mayúsculas y números), perfil (nombre y teléfono; el correo no se edita), cambio de contraseña, recuperación con código de 6 dígitos de un solo uso y 15 minutos (configurable), gestión de usuarios y roles por el administrador y eliminación lógica que se niega si el usuario es titular de reservas activas (`TitularReservasService`). |
| 7.3 Alojamiento y apartamentos | Agregado **Alojamiento** (datos, ubicación, normas y servicios adicionales con cargo). Apartamentos con descripción y características; listados que ignoran los retirados; detalle público; al bajar la capacidad se advierte y se listan las reservas que la superan, sin tocarlas. |
| 7.4 Tarifas | Histórico de tarifas (`RegistroTarifa`) y endpoint para consultarlo. |
| 7.5 Disponibilidad y reservas | Las **seis validaciones en el orden del enunciado** (`DisponibilidadApartamentoService.verificarDisponible`), búsqueda con fechas de nacimiento, filtros de capacidad, precio y características y **valor estimado** por tarjeta; **modificación de fechas, acompañantes o apartamento** (`Reserva.modificar`); filtros por canal y titular; **calendario de ocupación**. |
| 7.6 Operación diaria | Llegadas y salidas previstas del día; novedades del apartamento con fecha, autor, descripción y gravedad. |
| 7.7 Folio | El cierre con saldo distinto de cero lo **autoriza solo el administrador** y queda registrado con **autor, motivo y momento** (`AutorizacionCierre`); los servicios del catálogo se cargan con su valor (L-17). |
| 12.3 Datos de prueba | Un usuario por rol, reservas en los seis estados y en los tres canales, un bloqueo vigente, un apartamento FUERA_DE_SERVICIO, el alojamiento con servicios e histórico de tarifas. |

Estos ajustes no cambian lo comprometido en la Entrega 1: se conservan los ocho casos de uso del diagrama con sus nombres (incluido `ConsultarReservasPorEstadoUseCase`), las reglas RN-02, RN-03, RN-04 y RN-06 en el mismo lugar del modelo (`Apartamento.admite`, constructor de `Estancia`, `Reserva.crear`, `Ocupante.esFacturableEn`), el solapamiento en `Estancia.seSolapaCon`, las reglas propias RP-01 a RP-03 y los valores de la Ficha del Alojamiento: tarifas vigentes (A.3), umbral de 10 años, horarios 15:00 y 11:00, 3 horas de preparación, 24 horas de plazo, anticipo del 30 %, máximo 1 mascota, tramos de cancelación 0 % / 30 % / 50 %, no-show del 50 % (a las 20:00, según la corrección del profesor), medios de pago y servicios adicionales con su cargo (lavandería $25.000 por solicitud, desayuno $25.000 por persona y parqueadero adicional $20.000 por noche vía RP-02). Las descripciones y características de los apartamentos son las de la Ficha (A.2). Las cinco normas de convivencia de la Ficha (A.4) se cargan al iniciar. Lo único que no viene en la Ficha (dirección exacta y coordenadas) está marcado en `data.sql` como dato de ejemplo.

Quedan para los cortes siguientes, porque dependen de integraciones externas: imágenes en servicio externo, foto de perfil, canales de venta con credencial y bitácora (7.8), notificaciones al titular y reportes (7.9).

## 2. Agregados e invariantes

Las referencias entre agregados son solo por identificador (`IdentificacionApartamento`, `CodigoReserva`, `VersionPolitica`). Ninguna transición usa un `cambiarEstado` genérico.

| Raíz | Invariantes que protege el agregado por sí solo | Condiciones que requieren coordinación (servicio de dominio) |
|---|---|---|
| **Reserva** | 1. Nace PENDIENTE con código, estancia, titular incluido en el grupo y facturable, valor y política congelados (RN-04, RN-06, RN-22, 3.2). 2. Solo transita por la máquina de estados de la sección 8 con métodos de negocio (RN-08): confirmar exige hora estimada (RN-09), la llegada no ocurre antes de la entrada (RN-10), el no-show solo desde las 20:00 y sin novedad pendiente (L-15), el vencimiento solo después del plazo (RN-21). 3. Historial cronológico e inmutable. 4. Máximo 1 mascota (L-16) y 2 vehículos sin placas repetidas (RP-02). 5. Una modificación (fechas, grupo o apartamento) solo ocurre en PENDIENTE o CONFIRMADA no iniciada, mantiene al titular en el grupo y conserva la política congelada (RN-22, 7.5). | Disponibilidad y solapamiento (RN-01, RN-07, RN-20) y capacidad (RN-02) → `DisponibilidadApartamentoService`; valor (RN-05, RP-03) → `CotizadorEstanciaService`; estancia mínima (RP-01) → `EstanciaMinimaTemporadaService`; anticipo (L-11) → `AnticipoReservaService`; retención (RN-13) → `RetencionCancelacionService`; cupos de parqueadero (RP-02) → `DisponibilidadParqueaderoService`; canal + identificador externo único (RN-19) → `ConciliacionCanalExternoService`; revalidación de una modificación (RN-14) → `ModificacionReservaService`; no eliminar al titular de reservas activas (7.2) → `TitularReservasService`. |
| **Apartamento** | 1. Identificación, nombre, dormitorios ≥ 1, capacidad válida (L-05) y características sin vacíos. 2. Tiene tarifa en las tres temporadas: no se registra ni se reactiva con tarifas incompletas (7.4). 3. Las transiciones operativas respetan `EstadoOperativo`; solo un apartamento activo y PREPARADO se ocupa (RN-11). 4. Cada tarifa corresponde al apartamento; todo cambio queda en el histórico. 5. El historial de novedades solo crece. | Identificación única (L-04) → `RegistroApartamentoService`; retiro sin reservas activas ni futuras (7.3) → `RetiroApartamentoService`; entrega al grupo con reserva confirmada (RN-10, RN-11) → `EntregaApartamentoService`. |
| **Folio** | 1. Nace con el cargo de alojamiento (7.7). 2. Saldo derivado = cargos − pagos (RN-15). 3. Movimientos inmutables y cronológicos; un folio cerrado no admite movimientos; no se devuelve más de lo pagado (RN-16). 4. Cerrar con saldo ≠ 0 exige una `AutorizacionCierre` con autor, motivo y momento (RN-17, 7.7). 5. El alojamiento se liquida una sola vez (reverso + penalidad). | Salida con folio cerrado (RN-17, 7.6) → `SalidaApartamentoService`; anticipo para confirmar (L-11) → `AnticipoReservaService`. |
| **Bloqueo** | 1. Identidad, apartamento, periodo y motivo obligatorios. 2. Solo se levanta un bloqueo vigente y con motivo (eliminación lógica). 3. Solo un bloqueo vigente afecta noches (RN-07). | No bloquear noches con reservas activas ni duplicar un bloqueo vigente (7.3) → `BloqueoApartamentoService`. |
| **PoliticaCancelacion** | 1. Versión ≥ 1 inmutable con hora de referencia. 2. Tramos que cubren desde 0 horas sin umbrales repetidos y porcentajes entre 0 y 100 (L-10). 3. La retención usa antelación exacta respecto de la entrada (RN-13). | Aplicar la versión congelada en cada reserva → `RetencionCancelacionService`. |
| **Alojamiento** | 1. Nombre, ciudad, dirección y ubicación válida (latitud y longitud). 2. Normas sin textos vacíos. 3. Servicios con código único; si generan cargo, valor positivo y, si no, valor cero (L-17). | Cargar un servicio al folio → `RegistrarServicioAdicionalUseCase` (catálogo + folio). |
| **Usuario** (aplicación, no dominio: 3.6) | 1. Identidad, correo, documento, fecha de nacimiento, nombre, teléfono y rol obligatorios. 2. Solo guarda la contraseña cifrada. 3. El registro público siempre crea un HUESPED; un usuario inactivo no inicia sesión. | Correo y documento únicos y política de contraseña → casos de uso de `application`; no eliminar a un titular con reservas activas → `TitularReservasService`. |
| **CalendarioTemporadas** (objeto de valor) | 1. Las temporadas no se solapan (L-08, 7.4). 2. La BASE cubre toda noche no asignada (7.4). 3. Cada noche tiene una sola temporada. | — |

## 3. Identidad, reemplazo y ciclo de vida en conceptos concretos

- **Reserva `RES-2025-00002` (entidad).** *Identidad:* si mañana Carlos cambia las fechas, sigue siendo `RES-2025-00002`; dos reservas con las mismas fechas y el mismo grupo son reservas distintas. *Ciclo de vida:* PENDIENTE → CONFIRMADA → EN_CURSO → FINALIZADA queda registrado en su historial con autor y momento, y el folio se identifica por ese código.
- **Estancia del 10 al 12 de diciembre (objeto de valor).** *Reemplazo:* modificar la salida al 13 no "edita" la estancia: `Reserva.modificar` recibe una `Estancia` nueva y un valor recalculado. Dos estancias 10→12 son iguales.
- **Capacidad 4 del APT-301 (objeto de valor).** `cambiarCapacidad(new Capacidad(3))` sustituye el valor; el apartamento conserva su identidad y las reservas ya creadas no cambian (7.3).
- **Ocupante 1094123456 (entidad interna).** Si corrige su nombre sigue siendo el mismo ocupante (`actualizarNombre`); su identidad es el documento y solo existe dentro de su reserva (se persiste con `@ElementCollection`).
- **Pago (entidad interna del folio).** Dos pagos de $70.200 en efectivo el mismo día son movimientos distintos: cada uno tiene `UUID`. Nunca se edita; una corrección es una devolución (pago negativo).
- **Política de cancelación versión 1 (entidad versionada).** Publicar la versión 2 no altera la 1: `RES-2025-00001` sigue mostrando `versionPolitica = 1` (lo verifica la colección, carpeta 3).
- **Tarifa ALTA del APT-401 (objeto de valor).** `actualizarTarifa` crea otra `Tarifa` y agrega un `RegistroTarifa` al histórico; las reservas conservan las tarifas de su `ValorCongelado`.
- **Autorización de cierre del folio de RES-2025-00006 (objeto de valor).** Es inmutable: registra quién autorizó, por qué y cuándo; no se edita.

## 4. Casos de uso: documentados frente a implementados

Todos están en `application.usecase`, con `@Service`, `@Transactional` (o `readOnly = true` en consultas) y reciben tipos del dominio. Los casos que cambian más de un agregado documentan la excepción a "un agregado = una transacción" en su Javadoc.

| Caso de uso (Entrega 1 / catálogo) | Clase | Endpoint |
|---|---|---|
| Registrar huésped (7.2) | `RegistrarHuespedUseCase` | `POST /auth/registro` |
| Iniciar sesión (7.2) | `IniciarSesionUseCase` | `POST /auth/login` |
| Recuperar contraseña (7.2) | `SolicitarRecuperacionContrasenaUseCase`, `RestablecerContrasenaUseCase` | `POST /auth/recuperacion`, `POST /auth/recuperacion/restablecer` |
| Perfil y contraseña (7.2) | `ConsultarPerfilUseCase`, `ActualizarPerfilUseCase`, `CambiarContrasenaUseCase` | `GET`/`PUT /usuarios/yo`, `PUT /usuarios/yo/contrasena` |
| Gestionar usuarios y roles (7.2) | `ListarUsuariosUseCase`, `CrearUsuarioUseCase`, `AsignarRolUseCase`, `EliminarUsuarioUseCase` | `GET`/`POST /usuarios`, `PUT /usuarios/{id}/rol`, `DELETE /usuarios/{id}` |
| Gestionar el alojamiento (7.3) | `ConsultarAlojamientoUseCase`, `ActualizarAlojamientoUseCase` | `GET`/`PUT /alojamiento` |
| Actualizar datos del apartamento (7.3) | `ActualizarDatosApartamentoUseCase` | `PUT /apartamentos/{id}` |
| Calendario de ocupación (7.5) | `ConsultarCalendarioOcupacionUseCase` | `GET /apartamentos/{id}/calendario` |
| Histórico de tarifas (7.4) | `ConsultarApartamentoUseCase` | `GET /apartamentos/{id}/tarifas/historial` |
| Novedades del apartamento (7.6) | `RegistrarNovedadApartamentoUseCase`, `ConsultarApartamentoUseCase` | `POST`/`GET /apartamentos/{id}/novedades` |
| Llegadas y salidas del día (7.6) | `ConsultarMovimientosDelDiaUseCase` | `GET /reservas/movimientos-del-dia` |
| Cargar servicio adicional (L-17) | `RegistrarServicioAdicionalUseCase` | `POST /folios/{codigo}/servicios` |
| Registrar apartamento (7.3) | `RegistrarApartamentoUseCase` | `POST /apartamentos` |
| Consultar / listar apartamentos | `ConsultarApartamentoUseCase`, `ListarApartamentosUseCase` | `GET /apartamentos/{id}`, `GET /apartamentos` |
| Cambiar capacidad (7.3) | `CambiarCapacidadApartamentoUseCase` | `PUT /apartamentos/{id}/capacidad` |
| Actualizar tarifa (7.4) | `ActualizarTarifaApartamentoUseCase` | `PUT /apartamentos/{id}/tarifas` |
| Retirar apartamento (catálogo: `RetirarApartamentoUseCase`) | `RetirarApartamentoUseCase` | `PUT /apartamentos/{id}/retirar` |
| Reactivar apartamento | `ReactivarApartamentoUseCase` | `PUT /apartamentos/{id}/reactivar` |
| Preparación del apartamento (7.6) | `IniciarPreparacionApartamentoUseCase`, `MarcarApartamentoPreparadoUseCase`, `DeclararFueraDeServicioUseCase`, `ReincorporarApartamentoUseCase` | `PUT /apartamentos/{id}/iniciar-preparacion`, `/marcar-preparado`, `/fuera-de-servicio`, `/reincorporar` |
| Crear bloqueo (catálogo: `CrearBloqueoUseCase`) | `RegistrarBloqueoUseCase` | `POST /apartamentos/{id}/bloqueos` |
| Listar / levantar bloqueos | `ListarBloqueosUseCase`, `LevantarBloqueoUseCase` | `GET /apartamentos/{id}/bloqueos`, `PUT /bloqueos/{id}/levantar` |
| Registrar / listar temporadas (7.4) | `RegistrarTemporadaUseCase`, `ListarTemporadasUseCase` | `POST /temporadas`, `GET /temporadas` |
| Publicar / consultar política (7.4) | `PublicarPoliticaCancelacionUseCase`, `ConsultarPoliticaVigenteUseCase` | `POST /politicas-cancelacion`, `GET /politicas-cancelacion/vigente` |
| Cotizar estancia (catálogo) | `CotizarEstanciaUseCase` | `POST /cotizaciones` |
| Buscar disponibles (7.5) | `BuscarApartamentosDisponiblesUseCase` | `GET /apartamentos/disponibles` |
| Crear reserva (E1) · Recibir reserva externa (catálogo, canal EXTERNO) | `CrearReservaUseCase` | `POST /reservas` |
| Consultar reservas por estado (E1) | `ConsultarReservasPorEstadoUseCase` (mismo nombre de la E1; acepta además los filtros opcionales de 7.5) y `ConsultarReservaUseCase` | `GET /reservas`, `GET /reservas/{codigo}` |
| Confirmar reserva (E1) | `ConfirmarReservaUseCase` | `PUT /reservas/{codigo}/confirmar` |
| Cancelar reserva (E1) | `CancelarReservaUseCase` | `PUT /reservas/{codigo}/cancelar` |
| Indicar hora de llegada (RN-09) | `IndicarHoraLlegadaUseCase` | `PUT /reservas/{codigo}/hora-llegada` |
| Informar / resolver novedad de llegada (L-15) | `InformarNovedadLlegadaUseCase`, `ResolverNovedadLlegadaUseCase` | `PUT /reservas/{codigo}/novedad-llegada`, `/resolver-novedad` |
| Modificar reserva (catálogo; fechas, acompañantes o apartamento) | `ModificarReservaUseCase` | `PUT /reservas/{codigo}/modificacion` |
| Solicitar vehículo adicional (catálogo) | `RegistrarVehiculoUseCase` | `POST /reservas/{codigo}/vehiculos` |
| Registrar llegada (E1) | `RegistrarLlegadaUseCase` | `PUT /reservas/{codigo}/registrar-llegada` |
| Registrar salida (E1) | `RegistrarSalidaUseCase` | `PUT /reservas/{codigo}/registrar-salida` |
| Declarar no-show (E1; catálogo: `RegistrarNoShowUseCase`) | `DeclararNoShowUseCase` | `PUT /reservas/{codigo}/no-show` |
| Vencer reservas pendientes (E1) | `VencerReservasPendientesUseCase` | Sin endpoint: lo dispara `VencimientoReservasProgramado` (`@Scheduled`, `sga.vencimiento.cron`) |
| Folio: consultar, pagar, cargar, cerrar (7.7) | `ConsultarFolioUseCase`, `RegistrarPagoUseCase`, `RegistrarCargoUseCase`, `CerrarFolioUseCase` | `GET /folios/{codigo}`, `POST /folios/{codigo}/pagos`, `POST /folios/{codigo}/cargos`, `PUT /folios/{codigo}/cerrar` |

"Consultar reservas por estado" conserva el nombre de la Entrega 1 (`ConsultarReservasPorEstadoUseCase`) y su filtro por estado; además acepta, opcionales, apartamento, canal, titular, rango de fechas, orden de la más reciente a la más antigua y paginación (7.5).

## 5. Reglas propias: razones y consecuencias

| Regla | Razón de negocio | Consecuencia en el sistema | Dónde vive |
|---|---|---|---|
| **RP-01** Estancia mínima de 2 noches si incluye Temporada Especial | En fechas de demanda especial una noche suelta fragmenta el calendario y deja noches invendibles entre reservas. | Cotizar o crear una reserva de 1 noche especial responde 409; con 2 noches se acepta. El mínimo es configuración (`noches-minimas-por-temporada.ESPECIAL=2`). | `EstanciaMinimaTemporadaService` + `CalendarioTemporadas` |
| **RP-02** Máximo 2 vehículos; el primero incluido, el segundo con cargo de $20.000/noche sujeto a cupo | El parqueadero es limitado: se garantiza un cupo por reserva y se cobra el uso adicional. | El tercer vehículo responde 409 (la reserva lo impide); el segundo se rechaza si alguna noche no tiene cupo y, si hay, genera un cargo `SERVICIO_ADICIONAL` por todas las noches. | `Reserva.registrarVehiculo` + `DisponibilidadParqueaderoService` |
| **RP-03** 10 % de descuento desde 7 noches | Incentiva estancias largas y la ocupación continua. | Se aplica antes de congelar el valor (RN-22): un cambio posterior del porcentaje no altera reservas existentes; modificar la estancia recalcula el descuento. | `CotizadorEstanciaService` → `ValorCongelado` |
| **L-16** Máximo 1 mascota | Facilita limpieza y convivencia sin dejar de ser pet-friendly. | Pedir 2 mascotas responde 409. | `Reserva.indicarMascotas` |

## 6. Matriz de trazabilidad ampliada

Cada regla indica dónde se implementa (siempre en el dominio), la prueba unitaria que la verifica y el endpoint que la expone con su respuesta 409 en la colección.

| Regla | Implementación | Prueba unitaria | Endpoint que la expone | Caso 409 en la colección |
|---|---|---|---|---|
| RN-01 Sin reservas activas solapadas | `DisponibilidadApartamentoService.verificarDisponible`, `Estancia.seSolapaCon` | `DisponibilidadApartamentoServiceTest`, `ReservasUseCasesTest.rechazaReservaSolapada` | `POST /reservas`, `GET /apartamentos/disponibles`, `PUT /reservas/{c}/modificacion` | 5 · RN-01 |
| RN-02 Capacidad | `Apartamento.admite`, `Capacidad`, `DisponibilidadApartamentoService` | `CapacidadTest`, `DisponibilidadApartamentoServiceTest.validaCapacidadIncluyendoNoFacturables` | `POST /reservas`, `GET /apartamentos/disponibles` | 5 · RN-02 |
| RN-03 Al menos una noche | `Estancia` (constructor) | `EstanciaTest` | `POST /reservas`, `GET /apartamentos/disponibles`, `POST /cotizaciones` | 4 y 5 · RN-03 |
| RN-04 Sin reservas hacia el pasado | `Reserva.crear` | `ReservaTest.debeRechazarUnaEntradaAnteriorALaFechaDeCreacion` | `POST /reservas` | 5 · RN-04 |
| RN-05 Valor noche por noche | `CotizadorEstanciaService.cotizar`, `CargoNoche`, `ValorCongelado` | `CotizadorEstanciaServiceTest` | `POST /cotizaciones`, `POST /reservas` | 4 · cotización sin facturables |
| RN-06 Ocupante facturable / titular facturable | `Ocupante.esFacturableEn`, `Reserva.crear` | `OcupanteTest`, `ReservaTest.debeRechazarUnTitularNoFacturable…` | `POST /reservas`, `POST /cotizaciones` | 5 · RN-06 |
| RN-07 Bloqueo vigente | `Bloqueo.afecta`, `DisponibilidadApartamentoService` | `DisponibilidadApartamentoServiceTest.bloqueoSolapadoImpideReserva…` | `POST /reservas`, `GET /apartamentos/disponibles` | 5 · RN-07 |
| RN-08 Transiciones permitidas | `EstadoReserva.puedeTransicionarA`, métodos de `Reserva` | `EstadoReservaTest`, `ReservaConfirmacionTest`, `ReservaCancelacionTest` | `PUT /reservas/{c}/confirmar`, `/cancelar`, `/no-show`, `/registrar-llegada` | 5 y 6 · RN-08 |
| RN-09 Hora estimada para confirmar | `Reserva.confirmar` | `ReservaConfirmacionTest.noDebeConfirmarSinHoraEstimadaDeLlegada` | `PUT /reservas/{c}/confirmar` | 5 · RN-09 |
| RN-10 Llegada desde la fecha de entrada y confirmada | `Reserva.registrarLlegada`, `EntregaApartamentoService` | `ReservaLlegadaTest`, `EntregaApartamentoServiceTest` | `PUT /reservas/{c}/registrar-llegada` | 6 · RN-10 |
| RN-11 Solo PREPARADO recibe un grupo | `Apartamento.marcarOcupado`, `EstadoOperativo` | `ApartamentoTest`, `RegistrarLlegadaUseCaseTest.noGuardaNingunAgregadoSiElApartamentoNoEstaPreparado` | `PUT /reservas/{c}/registrar-llegada`, `PUT /apartamentos/{id}/iniciar-preparacion` | 3 · transición operativa inválida |
| RN-12 Libera noches al dejar de estar activa | `EstadoReserva.retieneDisponibilidad` | `DisponibilidadApartamentoServiceTest.reservaCanceladaLiberaNoches` | `PUT /reservas/{c}/cancelar` | — (efecto observable en disponibles) |
| RN-13 Retención con la política congelada | `RetencionCancelacionService`, `PoliticaCancelacion` | `PoliticaCancelacionTest`, `ReservasUseCasesTest.cancelaUsandoPoliticaHistorica…` | `PUT /reservas/{c}/cancelar`, `/no-show` | 5 · RN-08 cancelar dos veces |
| RN-14 / 7.5 Modificar repite las seis validaciones y registra ajuste | `ModificacionReservaService`, `Reserva.modificar` | `ServiciosEntrega2Test.modificarElGrupo…`, `…modificarElApartamento…`, `ReservaOperacionesAdicionalesTest` | `PUT /reservas/{c}/modificacion` | 5 · capacidad en la modificación |
| RN-15 Saldo = cargos − pagos | `Folio.saldo` | `FolioTest.pagosParcialesYDevolucion…` | `GET /folios/{c}`, `POST /folios/{c}/pagos` | — |
| RN-16 Movimientos inmutables; no devolver más de lo pagado | `Folio.registrarPago`, `Folio.registrarCargo` | `FolioTest.noDevuelveMasDeLoPagado` | `POST /folios/{c}/pagos`, `/cargos` | 5 · RN-16; 6 · folio cerrado |
| RN-17 / 7.7 Cierre con saldo solo con autorización del administrador (autor y motivo) | `Folio.cerrar`, `AutorizacionCierre`, `CerrarFolioUseCase`, `SalidaApartamentoService` | `FolioTest.cierreConSaldoRequiereAutorizacion`, `ApiSeguridadYErroresTest.soloElAdministrador…` | `PUT /folios/{c}/cerrar`, `PUT /reservas/{c}/registrar-salida` | 6 · RN-17 (403 recepción, 409 sin motivo) |
| RN-18 Reserva externa en conflicto se rechaza | `DisponibilidadApartamentoService` | `ReservasUseCasesTest.rechazaReservaSolapada` | `POST /reservas` (EXTERNO) | 5 · RN-01 |
| RN-19 Canal + identificador externo único | `ConciliacionCanalExternoService` | `ServiciosEntrega2Test.conciliacionRechaza…` | `POST /reservas` | 5 · RN-19 |
| RN-20 Tiempo de preparación | `DisponibilidadApartamentoService`, `TiempoPreparacion` | `DisponibilidadApartamentoServiceTest.preparacionSeValida…` | `POST /reservas`, `GET /apartamentos/disponibles` | — (con 11:00 → 15:00 y 3 h no hay choque posible; se prueba en unidad) |
| RN-21 Vencimiento de PENDIENTE | `Reserva.vencer`, `VencerReservasPendientesUseCase` | `ReservaVencimientoTest` | Sin endpoint (`@Scheduled`) y `PUT /reservas/{c}/confirmar` | — |
| RN-22 Valor y política congelados | `Reserva.crear`, `ValorCongelado` | `ReservaTest.debeConservarElDesglose…` | `POST /reservas`, `POST /politicas-cancelacion` | 3 · RN-22 |
| L-04 Identificación única | `RegistroApartamentoService` | `ServiciosEntrega2Test.registroDeApartamentoExigeIdentificacionUnica` | `POST /apartamentos` | 3 · L-04 |
| L-05 Capacidad válida | `Capacidad` | `CapacidadTest` | `POST /apartamentos`, `PUT /apartamentos/{id}/capacidad` | 3 · 400 dormitorios |
| L-08 Temporadas sin ambigüedad | `CalendarioTemporadas`, `Temporada` | `CalendarioTemporadasTest` | `POST /temporadas` | 3 · L-08 |
| 7.4 Tarifas completas | `Apartamento` (constructor y `reactivar`) | `ApartamentoTest.noDebeRegistrarseActivoSinTarifas…` | `POST /apartamentos`, `PUT /apartamentos/{id}/reactivar` | 3 · 7.4 |
| 7.3 Retiro sin reservas activas | `RetiroApartamentoService` | `ServiciosEntrega2Test.retiroRechaza…` | `PUT /apartamentos/{id}/retirar` | 3 · 7.3 |
| 7.3 Bloqueo sin reservas activas | `BloqueoApartamentoService` | `ServiciosEntrega2Test.bloqueoRechaza…` | `POST /apartamentos/{id}/bloqueos` | 3 · 7.3 bloqueo |
| L-09 Umbral de edad configurable (10) | `UmbralEdadFacturable`, `sga.alojamiento.umbral-edad-facturable` | `UmbralEdadFacturableTest` | `POST /cotizaciones` | — |
| L-10 Tramos de la política | `PoliticaCancelacion`, `TramoCancelacion` | `PoliticaCancelacionTest` | `POST /politicas-cancelacion` | 3 · L-10 |
| L-11 Anticipo del 30 % | `AnticipoReservaService` | `ReservasUseCasesTest.rechazaAnticipoInsuficienteSinCambios` | `PUT /reservas/{c}/confirmar` | 5 · L-11 |
| L-13 Preparación de 3 horas | `TiempoPreparacion`, `sga.alojamiento.tiempo-preparacion-horas` | `TiempoPreparacionTest` | `POST /reservas` | — |
| L-14 Plazo de confirmación de 24 h | `PlazoConfirmacion`, `Reserva.confirmar(..., plazo)` | `ReservaOperacionesAdicionalesTest.confirmacionExactaAlLimite…` | `PUT /reservas/{c}/confirmar` | — |
| L-15 No-show desde las 20:00 sin novedad | `Reserva.declararNoShow` | `ReservasUseCasesTest`, `ReservaOperacionesAdicionalesTest` | `PUT /reservas/{c}/no-show` | 6 · L-15 |
| L-16 Máximo 1 mascota | `Reserva.indicarMascotas` | `ReservaEntrega2Test.rechazarMasDeUnaMascota` | `POST /reservas` | 5 · L-16 |
| L-17 Servicios adicionales y su cargo | `ServicioAdicional.cargoPor`, `Alojamiento.servicio`, `Folio.registrarCargo` | `NuevosValoresTest.servicioAdicionalConCargo…`, `FolioTest` | `PUT /alojamiento`, `POST /folios/{c}/servicios` | 3 · L-17; 6 · servicio sin cargo |
| 7.2 No eliminar titular con reservas activas | `TitularReservasService` | `ServiciosEntrega2Test.unTitularConReservasActivas…` | `DELETE /usuarios/{id}` | 1b · 7.2 |
| 7.2 Contraseña segura | `PoliticaContrasena`, Bean Validation `@Pattern` | `UsuarioTest.laContrasenaDebeSerSegura` | `POST /auth/registro`, `PUT /usuarios/yo/contrasena` | 1 · 400 contraseña débil |
| 7.3 Cambio de capacidad no afecta reservas: advierte | `CambiarCapacidadApartamentoUseCase` | — (lo cubre la colección) | `PUT /apartamentos/{id}/capacidad` | 3 · advertencia (200) |
| L-18 Medios de pago admitidos | `MedioPago` | — (enum cerrado) | `POST /folios/{c}/pagos` | 5 · 400 medio no admitido |
| RP-01 Estancia mínima en Especial | `EstanciaMinimaTemporadaService` | `ReservasUseCasesTest.rechazaUnaNocheEspecialSinGuardarNada` | `POST /cotizaciones`, `POST /reservas` | 4 y 5 · RP-01 |
| RP-02 Vehículos | `Reserva.registrarVehiculo`, `DisponibilidadParqueaderoService` | `ReservaEntrega2Test`, `ServiciosEntrega2Test.parqueadero…` | `POST /reservas/{c}/vehiculos` | 5 · RP-02 |
| RP-03 Descuento desde 7 noches | `CotizadorEstanciaService` | `CotizadorEstanciaServiceTest.debeAplicarDiezPorCiento…` | `POST /cotizaciones`, `POST /reservas` | — (200 con descuento) |

## 7. Arquitectura y decisiones técnicas

- **Dominio puro.** `domain` no importa Spring, JPA ni Lombok. Las entidades tienen constructor controlado, sin setters e igualdad por identidad; los objetos de valor son `record` o `enum` con validación en el constructor. La persistencia usa los métodos `reconstruir(...)`, que no repiten las validaciones de creación (Guía 10).
- **Materialización.** Casos de uso con `@Service` y `@Transactional`; servicios de dominio registrados con `@Bean` en `DomainServiceConfig`; parámetros de la Ficha en `application.properties` (`sga.alojamiento.*`) leídos por `PropiedadesAlojamiento`; un único `Clock` en `TiempoConfig`.
- **Persistencia H2.** Un adaptador JPA por puerto (`infrastructure.persistence.jpa`): entidades JPA separadas, `@Embeddable` para objetos de valor y entidades internas (ocupantes, cargos, pagos, historial, tarifas, tramos), mappers MapStruct y repositorios Spring Data. Eliminación lógica: apartamento retirado (`activo = false`), bloqueo levantado (`vigente = false`), usuario inactivo; las reservas y movimientos nunca se borran. Los adaptadores en memoria siguen en el repositorio para las pruebas, pero no son beans.
- **Datos de prueba (12.3).** `data.sql` carga un usuario por rol (más un segundo huésped), el alojamiento con normas y servicios, los 7 apartamentos de la Ficha con descripción, características e histórico de tarifas (APT-202 FUERA_DE_SERVICIO), temporadas, la política versión 1, un bloqueo vigente y 7 reservas que cubren los seis estados y los tres canales, con sus folios. Las fechas son relativas a `CURRENT_DATE`. `ZonaHorariaEnvironmentPostProcessor` fija la zona de la JVM con `sga.alojamiento.zona-horaria` antes de crear el contexto, así la base, JDBC y el reloj usan la misma zona en cualquier máquina (antes, una zona distinta en la URL de H2 corría un día las fechas guardadas; lo cubre `ApiSeguridadYErroresTest.lasFechasGuardadasSeLeenIguales`).
- **API.** DTOs `record` en `application.dto.request/response`, que solo exponen enums cerrados del dominio. Bean Validation valida la forma (400); el dominio valida las reglas (409). Un controller por recurso, sin endpoint genérico de estado; el único `DELETE` es la eliminación lógica de usuarios.
- **Excepciones.** `ReglaDominioException` (dominio) → 409; `RecursoNoEncontradoException` y sus subclases (aplicación) → 404; `AccesoDenegadoException` → 403; `CredencialesInvalidasException` → 401; `GlobalExceptionHandler` y `RespuestaErrorSeguridad` producen un único `ErrorResponse` sin trazas.
- **Correo.** `NotificadorCorreo` es un puerto de aplicación; `NotificadorCorreoLocal` (I-02) escribe el mensaje en el log, de modo que la recuperación de contraseña funciona sin internet ni credenciales, y un fallo de envío no detiene la operación (I-05).
- **Seguridad.** JWT HS256 con expiración; la clave llega por `SGA_JWT_SECRETO` (si falta, se genera una aleatoria al arrancar), nunca está en el repositorio. Contraseñas con BCrypt. El rol se verifica en `SeguridadConfig`; el propietario, en los casos de uso con `Solicitante.verificarAcceso` y `Reserva.perteneceA`.

### Matriz de roles (7.1)

| Recurso / operación | Público | HUESPED | PERSONAL_SERVICIO | RECEPCIONISTA | ADMINISTRADOR |
|---|:-:|:-:|:-:|:-:|:-:|
| Registro, inicio de sesión y recuperación de contraseña | ✔ | | | | |
| Alojamiento, disponibles, detalle y calendario del apartamento, cotización, política vigente | ✔ | ✔ | ✔ | ✔ | ✔ |
| Mi perfil y cambio de contraseña | | ✔ | ✔ | ✔ | ✔ |
| Gestionar usuarios y roles; actualizar el alojamiento | | | | | ✔ |
| Registrar y editar apartamento, capacidad, tarifas, retirar, reactivar, fuera de servicio | | | | | ✔ |
| Listar apartamentos, iniciar y terminar la preparación, novedades | | | ✔ | ✔ | ✔ |
| Histórico de tarifas, listar bloqueos y temporadas | | | | ✔ | ✔ |
| Registrar bloqueo y levantarlo; temporada; política | | | | | ✔ |
| Crear reserva | | PORTAL, a su nombre | | DIRECTO / EXTERNO | DIRECTO / EXTERNO |
| Consultar, listar, cancelar, hora de llegada, novedad de llegada, vehículos | | solo las suyas | | ✔ | ✔ |
| Modificar, confirmar, resolver novedad, llegada, salida, no-show, movimientos del día | | | | ✔ | ✔ |
| Consultar folio | | solo el suyo | | ✔ | ✔ |
| Pagos, cargos, servicios y cierre del folio con saldo cero | | | | ✔ | ✔ |
| Autorizar el cierre del folio con saldo | | | | | ✔ |

## 8. Recorrido completo de una situación

*Marta Restrepo llama a recepción para reservar el APT-102 (Guadua) del día +40 al +43 con su hijo Tomás de 6 años.*

1. **HTTP.** Recepción envía `POST /api/reservas` con `Authorization: Bearer …` y el `CrearReservaRequest` (titular Marta, acompañante Tomás, canal DIRECTO).
2. **Seguridad.** `SeguridadConfig` valida el JWT; `SolicitanteArgumentResolver` construye el `Solicitante` (correo de recepción, rol RECEPCIONISTA) a partir del token.
3. **Forma (400).** `@Valid` verifica campos obligatorios, fechas y tamaños. Si faltara el titular, `GlobalExceptionHandler` respondería 400 con `detalles`.
4. **Traducción.** `ReservaRestMapper` convierte el DTO en `Ocupante`, `Estancia` e `IdentificacionApartamento`; un `Estancia(+43, +40)` lanzaría `ReglaDominioException` (RN-03 → 409).
5. **Caso de uso.** `CrearReservaUseCase` (`@Transactional`): `Solicitante.verificarPuedeReservar` comprueba que el canal DIRECTO corresponde a un rol del personal (403 si un huésped lo intenta); carga el apartamento (404 si no existe); consulta los servicios de dominio: `ConciliacionCanalExternoService` (RN-19), `DisponibilidadApartamentoService` (las validaciones 2 a 6 de 7.5 en orden: entrada no pasada, activo con tarifas completas, capacidad, sin solapamientos ni bloqueos, preparación), `CotizadorEstanciaService` (RN-05: Tomás no es facturable por tener menos de 10 años, RN-06), `EstanciaMinimaTemporadaService` (RP-01); y toma la versión vigente de la política.
6. **Dominio.** `Reserva.crear` decide: nace PENDIENTE, Marta es titular facturable incluida en el grupo, la entrada no es pasada (RN-04), el valor y la política quedan congelados (RN-22). Se abre el `Folio` con el cargo de alojamiento.
7. **Persistencia.** `ReservaJpaRepository` y `FolioJpaRepository` traducen a `ReservaEntity`/`FolioEntity` con MapStruct y guardan en H2 en la misma transacción (F-08).
8. **Respuesta.** `201 Created` con `Location: /api/reservas/RES-2026-000NN` y el `ReservaDetalleResponse`.
9. **Siguiente paso.** Confirmar sin pago responde **409** (L-11); tras registrar el anticipo del 30 % sin hora de llegada, **409** (RN-09); con la hora, **200 CONFIRMADA**; confirmar otra vez, **409** (RN-08). Es la carpeta 5 de la colección.

## 9. Cómo verificar

```bash
./gradlew test
```

```bash
./gradlew bootRun
```

Luego, con la aplicación recién arrancada, importar la colección en Postman y ejecutarla completa (Runner), o con Newman:

```bash
npx newman run docs/pruebas/SGA-Entrega2.postman_collection.json
```

Resultado verificado el 31 de octubre de 2026: 489 pruebas de JUnit sin fallos (26 nuevas con Mockito para crear, cancelar, modificar, salida, no-show y vencimiento, más seis de contrato y validación HTTP), y 179 solicitudes con 334 aserciones de Newman sin fallos sobre una aplicación recién arrancada.

El [plan de cierre](PLAN_CIERRE_ENTREGA_2.md) registra la revisión de la rúbrica, los datos pendientes y la propuesta de reparto para el equipo.

En local, el código de recuperación de contraseña aparece en la consola de la aplicación (`[correo local] ... Código de recuperación: NNNNNN`).

El [contraste con la Entrega 1](REVISION_ENTREGA_1.md) relaciona los 31 conceptos y los 12 servicios del Excel local con las clases actuales y explica los cambios de firma y nombre. Debe confirmarse que ese catálogo sea la versión definitiva, ya que la retroalimentación menciona trece servicios.

### Revisión adicional del contrato y Swagger

`ContratoOpenApiTest` verifica métodos y rutas, nombres de campos de los DTOs,
valores de los enums y presencia de los campos obligatorios de los requests.
También comprueba que Swagger no pida `solicitante`: el servidor lo obtiene del
JWT. Se corrigió su aparición en los tres endpoints del perfil.

Los requests de alta/edición de apartamento, cambio de capacidad, servicio de
folio, servicio del catálogo y tramo de política usan envoltorios con `@NotNull`
para los campos obligatorios numéricos y booleanos. Así, un dato ausente no se
transforma silenciosamente en 0 o false. Las dos regresiones HTTP comprueban
400 al omitir `generaCargo` o `antelacionMinimaHoras`. El contrato ya declaraba
esos campos obligatorios, por lo que esta corrección alinea la implementación.
