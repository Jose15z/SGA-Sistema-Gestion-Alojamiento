# Correspondencia de la Entrega 1 con el código actual

Revisión estructural del 30 de octubre de 2026 del archivo original
`Plantilla_Identificacion_Entidad_ObjetoValor_actualizada.xlsx`.
Se conserva el Excel sin modificar; estas tablas actualizan su correspondencia
con las clases y firmas actuales. La presencia de una clase no sustituye la
verificación de sus reglas mediante las pruebas y la matriz de `ENTREGA_2.md`.

## Conceptos de la hoja Identificacion

Se localizaron los 31 conceptos: 7 entidades y 24 objetos de valor.
Las entidades conservan igualdad y hash por identidad; los objetos de valor
son records o enums. Los constructores y reglas se prueban en las suites de dominio.

| Fila de E1 | Concepto | Clasificación | Clase actual en domain |
|---:|---|---|---|
| 7 | Reserva | Entidad | `entity/Reserva` (class) |
| 8 | Apartamento | Entidad | `entity/Apartamento` (class) |
| 9 | Estancia | Objeto de Valor | `valueobject/Estancia` (record) |
| 10 | Capacidad | Objeto de Valor | `valueobject/Capacidad` (record) |
| 11 | Estado de la reserva | Objeto de Valor | `valueobject/EstadoReserva` (enum) |
| 12 | Ocupante | Entidad | `entity/Ocupante` (class) |
| 13 | Temporada | Objeto de Valor | `valueobject/Temporada` (record) |
| 14 | Tarifa | Objeto de Valor | `valueobject/Tarifa` (record) |
| 15 | Folio | Entidad | `entity/Folio` (class) |
| 16 | Pago | Entidad | `entity/Pago` (class) |
| 17 | Canal de origen | Objeto de Valor | `valueobject/CanalOrigen` (enum) |
| 18 | Bloqueo | Entidad | `entity/Bloqueo` (class) |
| 19 | Código de reserva | Objeto de Valor | `valueobject/CodigoReserva` (record) |
| 20 | Identificación del apartamento | Objeto de Valor | `valueobject/IdentificacionApartamento` (record) |
| 21 | Documento de identidad | Objeto de Valor | `valueobject/DocumentoIdentidad` (record) |
| 22 | Estado operativo | Objeto de Valor | `valueobject/EstadoOperativo` (enum) |
| 23 | Dinero | Objeto de Valor | `valueobject/Dinero` (record) |
| 24 | Umbral de edad facturable | Objeto de Valor | `valueobject/UmbralEdadFacturable` (record) |
| 25 | Política de Cancelación | Entidad | `entity/PoliticaCancelacion` (class) |
| 26 | Valor Congelado | Objeto de Valor | `valueobject/ValorCongelado` (record) |
| 27 | Versión de Política | Objeto de Valor | `valueobject/VersionPolitica` (record) |
| 28 | Identificador Externo | Objeto de Valor | `valueobject/IdentificadorExterno` (record) |
| 29 | Capacidad de Parqueadero | Objeto de Valor | `valueobject/CapacidadParqueadero` (record) |
| 30 | Fecha de creación | Objeto de Valor | `valueobject/FechaCreacion` (record) |
| 31 | Cargo | Objeto de Valor | `valueobject/Cargo` (record) |
| 32 | Cargo por noche | Objeto de Valor | `valueobject/CargoNoche` (record) |
| 33 | Evento de reserva | Objeto de Valor | `valueobject/EventoReserva` (record) |
| 34 | Plazo de confirmación | Objeto de Valor | `valueobject/PlazoConfirmacion` (record) |
| 35 | Tiempo de preparación | Objeto de Valor | `valueobject/TiempoPreparacion` (record) |
| 36 | Tramo de cancelación | Objeto de Valor | `valueobject/TramoCancelacion` (record) |
| 37 | Tipo de cargo | Objeto de Valor | `valueobject/TipoCargo` (enum) |

## Servicios del catálogo

La copia local del catálogo tiene 12 filas de servicios, todas con clase actual
y registro en `DomainServiceConfig`. El código tiene además
`RegistroApartamentoService` y `TitularReservasService` (14 servicios en total).
La retroalimentación menciona trece; conviene confirmar cuál versión del catálogo
se entregó. No se infiere que falte un servicio por esa diferencia de conteo.

| Servicio del catálogo | Firma actual resumida | Evolución respecto al Excel |
|---|---|---|
| `DisponibilidadApartamentoService` | `verificarDisponible(...)` | Añade reserva excluida y fecha actual para modificar sin competir consigo misma y validar el pasado. |
| `CotizadorEstanciaService` | `ValorCongelado cotizar(...)` | Retorna el desglose por noche, no solo Dinero; se congela en la reserva. |
| `RetencionCancelacionService` | `calcular(reserva, LocalDateTime)`, `calcularNoShow(reserva)` | Usa la política histórica y antelación en horas; distingue cancelación de no-show. |
| `EntregaApartamentoService` | `Apartamento autorizarEntrega(reserva, fechaActual)` | Devuelve el apartamento autorizado; el caso de uso coordina los cambios. |
| `ModificacionReservaService` | `ValorCongelado validarModificacion(...)` | Compone disponibilidad, cotización y estancia mínima; devuelve la nueva cotización. El caso de uso persiste y ajusta el folio. |
| `ConciliacionCanalExternoService` | `verificarNoDuplicada(canal, IdentificadorExterno)` | Verifica RN-19. El caso de uso de creación verifica también disponibilidad para el canal EXTERNO (RN-18). |
| `EstanciaMinimaTemporadaService` | `verificar(ValorCongelado)` | Reutiliza las temporadas ya cotizadas y consulta ConfiguracionAlojamiento; no necesita consultar de nuevo el tarifario. |
| `DisponibilidadParqueaderoService` | `Dinero verificarVehiculoAdicional(reserva)` | Devuelve el cargo del vehículo adicional tras comprobar cupo; el caso de uso lo registra en el folio. |
| `BloqueoApartamentoService` | `validarBloqueo(apartamentoId, periodo)` | Mantiene la intención del catálogo. |
| `RetiroApartamentoService` | `validarRetiro(apartamento)` | Mantiene la intención del catálogo. |
| `AnticipoReservaService` | `verificar(reserva, folio)` | Comprueba anticipo configurado y correspondencia de agregados. |
| `SalidaApartamentoService` | `verificar(reserva, apartamento, folio)` | Comprueba correspondencia, estados y folio cerrado antes de la salida. |

## Nombres de casos de uso adicionales

| Nombre previsto en el catálogo | Implementación actual | Exposición |
|---|---|---|
| `RecibirReservaExternaUseCase` | `CrearReservaUseCase` con canal EXTERNO | `POST /reservas` |
| `SolicitarVehiculoAdicionalUseCase` | `RegistrarVehiculoUseCase` | `POST /reservas/{codigo}/vehiculos` |
| `RegistrarNoShowUseCase` | `DeclararNoShowUseCase` | `PUT /reservas/{codigo}/no-show` |
| `CotizarEstanciaUseCase` | Mismo nombre | `POST /cotizaciones` |
| `ModificarReservaUseCase` | Mismo nombre | `PUT /reservas/{codigo}/modificacion` |
| `CrearBloqueoUseCase` | Mismo nombre | `POST /apartamentos/{identificacion}/bloqueos` |
| `RetirarApartamentoUseCase` | Mismo nombre | `PUT /apartamentos/{identificacion}/retirar` |

La unificación de la reserva externa en creación es una decisión de implementación
documentada: se ejecutan tanto la conciliación de duplicados como la disponibilidad.
No hay un endpoint adicional de integración externa en este corte.

## Límites de esta revisión

El diagrama actual y la matriz de reglas están en `docs/diagramas/DiagramaDeClases.md`
y `docs/ENTREGA_2.md`. La revisión confirma presencia, clasificación y correspondencia
de los servicios del Excel disponible. El equipo debe validar que esta sea la versión
definitiva entregada y explicar en la sustentación las firmas que evolucionaron.
No se modifica el Excel original ni se considera prueba de cada regla la sola
existencia de una clase.
