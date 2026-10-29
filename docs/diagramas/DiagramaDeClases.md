# Diagrama de clases — SGA · Entrega 2

Actualiza el diagrama de la Entrega 1 con los cambios del dominio: `Temporada`, `CalendarioTemporadas` y `TipoTemporada`; tarifas dentro de `Apartamento`; `IdentificadorExterno`, mascotas y vehículos en `Reserva`; `MedioPago`; `CapacidadParqueadero`; los servicios del catálogo que faltaban y los nuevos puertos. En la revisión contra el documento del proyecto se agregaron el agregado `Alojamiento` (con `ServicioAdicional` y `Ubicacion`), el histórico de tarifas y las novedades del `Apartamento`, la `AutorizacionCierre` del folio, la modificación general de la reserva y el calendario de ocupación. `Usuario`, `Rol` y `CorreoElectronico` salieron del dominio (3.6) y se muestran en el tercer diagrama.

Versiones en imagen: [dominio](DiagramaDeClases-dominio.svg), [servicios y puertos](DiagramaDeClases-servicios.svg) y [acceso y seguridad](DiagramaDeClases-seguridad.svg).

Convenciones: composición `*--`, dependencia o referencia por identificador `..>`, implementación `..|>`. Se abrevian parámetros con `...`.

## Entidades, agregados y objetos de valor

```mermaid
classDiagram
direction TB
  class Reserva {
    <<aggregate_root>>
    -CodigoReserva codigo
    -IdentificacionApartamento apartamento
    -CanalOrigen canalOrigen
    -IdentificadorExterno identificadorExterno
    -FechaCreacion fechaCreacion
    -Ocupante titular
    -VersionPolitica politica
    -Estancia estancia
    -List~Ocupante~ ocupantes
    -ValorCongelado valor
    -EstadoReserva estado
    -List~EventoReserva~ historial
    -LocalTime horaEstimadaLlegada
    -String novedadLlegada
    -int mascotas
    -List~Vehiculo~ vehiculos
    +crear(...)$ Reserva
    +reconstruir(...)$ Reserva
    +indicarHoraEstimadaLlegada(...) void
    +indicarMascotas(...) void
    +registrarVehiculo(...) void
    +siguienteVehiculoEsAdicional(int) boolean
    +confirmar(...) void
    +cancelar(...) void
    +registrarLlegada(...) void
    +registrarSalida(...) void
    +informarNovedadLlegada(...) void
    +resolverNovedadLlegada(...) void
    +declararNoShow(...) void
    +vencer(...) void
    +estaVencida(...) boolean
    +modificarEstancia(...) Dinero
    +modificar(apartamento, estancia, ocupantes, ...) Dinero
    +perteneceA(DocumentoIdentidad) boolean
    +totalOcupantes() int
  }
  class Ocupante {
    <<entity>>
    -DocumentoIdentidad documento
    -LocalDate fechaNacimiento
    -String nombre
    +reconstruir(...)$ Ocupante
    +edadA(LocalDate) int
    +esFacturableEn(Estancia, UmbralEdadFacturable) boolean
    +actualizarNombre(String) void
  }
  class Apartamento {
    <<aggregate_root>>
    -IdentificacionApartamento identificacion
    -String nombre
    -String descripcion
    -int dormitorios
    -Capacidad capacidad
    -List~String~ caracteristicas
    -EstadoOperativo estadoOperativo
    -boolean activo
    -List~Tarifa~ tarifas
    -List~RegistroTarifa~ historialTarifas
    -List~NovedadApartamento~ novedades
    +reconstruir(...)$ Apartamento
    +actualizarDatos(...) void
    +tieneCaracteristicas(List) boolean
    +registrarNovedad(NovedadApartamento) void
    +admite(int) boolean
    +puedeRecibirGrupo() boolean
    +tieneTarifasCompletas() boolean
    +actualizarTarifa(TipoTemporada, Dinero, LocalDateTime) void
    +tarifaPara(TipoTemporada) Tarifa
    +marcarOcupado() void
    +liberar() void
    +iniciarPreparacion() void
    +marcarPreparado() void
    +declararFueraDeServicio() void
    +reincorporarAPreparacion() void
    +cambiarCapacidad(Capacidad) void
    +desactivar() void
    +reactivar() void
  }
  class Folio {
    <<aggregate_root>>
    -CodigoReserva reserva
    -List~Cargo~ cargos
    -List~Pago~ pagos
    -boolean cerrado
    -boolean alojamientoLiquidado
    -AutorizacionCierre autorizacionCierre
    +reconstruir(...)$ Folio
    +registrarCargo(Cargo) void
    +registrarPago(Pago) void
    +verificarLiquidacion(...) void
    +liquidarAlojamiento(...) void
    +saldo() Dinero
    +totalPagado() Dinero
    +cerrar() void
    +cerrar(AutorizacionCierre) void
    +requiereAutorizacionParaCerrar() boolean
  }
  class Pago {
    <<entity>>
    -UUID identificacion
    -Dinero valor
    -MedioPago medio
    -LocalDateTime momento
    -String concepto
    -String autor
  }
  class Bloqueo {
    <<aggregate_root>>
    -UUID identificacion
    -IdentificacionApartamento apartamento
    -Estancia periodo
    -String motivo
    -boolean vigente
    -String motivoLevantamiento
    +reconstruir(...)$ Bloqueo
    +afecta(Estancia) boolean
    +levantar(String) void
  }
  class PoliticaCancelacion {
    <<aggregate_root>>
    -VersionPolitica version
    -List~TramoCancelacion~ tramos
    -BigDecimal porcentajeNoShow
    -LocalTime horaEntrada
    +retencionPorCancelacion(...) Dinero
    +retencionPorNoShow(Dinero) Dinero
    +siguienteVersion(...) PoliticaCancelacion
  }
  class Alojamiento {
    <<aggregate_root>>
    -UUID identificacion
    -String nombre
    -String descripcion
    -String ciudad
    -String direccion
    -Ubicacion ubicacion
    -List~String~ normas
    -List~ServicioAdicional~ servicios
    +actualizarDatos(...) void
    +definirServicios(List) void
    +servicio(String) ServicioAdicional
  }
  class ServicioAdicional {
    <<value_object>>
    -String codigo
    -String nombre
    -boolean generaCargo
    -Dinero valor
    -UnidadCobro unidad
    +cargoPor(int) Dinero
  }
  class Ubicacion {
    <<value_object>>
    -BigDecimal latitud
    -BigDecimal longitud
  }
  class RegistroTarifa {
    <<value_object>>
    -TipoTemporada temporada
    -Dinero valor
    -LocalDateTime vigenteDesde
  }
  class NovedadApartamento {
    <<value_object>>
    -LocalDateTime fecha
    -String autor
    -String descripcion
    -GravedadNovedad gravedad
  }
  class AutorizacionCierre {
    <<value_object>>
    -String autor
    -String motivo
    -LocalDateTime momento
  }
  class NocheCalendario {
    <<value_object>>
    -LocalDate noche
    -EstadoNoche estado
    -CodigoReserva reserva
  }
  class CalendarioTemporadas {
    <<value_object>>
    -List~Temporada~ temporadas
    +tipoDe(LocalDate) TipoTemporada
    +con(Temporada) CalendarioTemporadas
  }
  class Temporada {
    <<value_object>>
    -String nombre
    -TipoTemporada tipo
    -LocalDate fechaInicio
    -LocalDate fechaFin
    +incluye(LocalDate) boolean
    +seSolapaCon(Temporada) boolean
  }
  class Tarifa {
    <<value_object>>
    -IdentificacionApartamento apartamento
    -TipoTemporada temporada
    -Dinero valorPorOcupanteNoche
  }
  class ValorCongelado {
    <<value_object>>
    -List~CargoNoche~ detalle
    -BigDecimal porcentajeDescuento
    +subtotal() Dinero
    +total() Dinero
    +descuento() Dinero
  }
  class CargoNoche {
    <<value_object>>
    -LocalDate noche
    -TipoTemporada temporada
    -Dinero tarifaPorOcupante
    -int ocupantesFacturables
  }
  class Estancia {
    <<value_object>>
    -LocalDate fechaEntrada
    -LocalDate fechaSalida
    +noches() int
    +seSolapaCon(Estancia) boolean
  }
  class Cargo {
    <<value_object>>
    -TipoCargo tipo
    -String concepto
    -Dinero valor
    -LocalDateTime momento
    -String autor
  }
  class EventoReserva {
    <<value_object>>
  }
  class Vehiculo {
    <<value_object>>
    -String placa
  }
  class IdentificadorExterno {
    <<value_object>>
    -String valor
  }
  class CapacidadParqueadero {
    <<value_object>>
    -int cupos
    +admite(int) boolean
  }
  class TipoTemporada {
    <<enumeration>>
    BASE
    ALTA
    ESPECIAL
  }
  class MedioPago {
    <<enumeration>>
    EFECTIVO
    TARJETA_DEBITO
    TARJETA_CREDITO
    TRANSFERENCIA_BANCARIA
  }
  class CanalOrigen {
    <<enumeration>>
    PORTAL
    DIRECTO
    EXTERNO
  }
  class EstadoNoche {
    <<enumeration>>
    DISPONIBLE
    RESERVADA
    BLOQUEADA
    EN_PREPARACION
    NO_VENDIBLE
  }
  class EstadoReserva {
    <<enumeration>>
    PENDIENTE
    CONFIRMADA
    EN_CURSO
    FINALIZADA
    CANCELADA
    NO_SHOW
  }
  class EstadoOperativo {
    <<enumeration>>
    PREPARADO
    OCUPADO
    PENDIENTE_PREPARACION
    EN_PREPARACION
    FUERA_DE_SERVICIO
  }
Reserva *-- Ocupante : ocupantes
Reserva *-- Estancia
Reserva *-- ValorCongelado
Reserva *-- EventoReserva : historial
Reserva *-- Vehiculo : vehiculos
Reserva *-- IdentificadorExterno
Reserva *-- CanalOrigen
Reserva *-- EstadoReserva
ValorCongelado *-- CargoNoche
CargoNoche *-- TipoTemporada
Apartamento *-- Tarifa : tarifas
Apartamento *-- EstadoOperativo
Tarifa *-- TipoTemporada
Folio *-- Cargo
Folio *-- Pago
Pago *-- MedioPago
Bloqueo *-- Estancia : periodo
CalendarioTemporadas *-- Temporada
Temporada *-- TipoTemporada
Apartamento *-- RegistroTarifa : historial
Apartamento *-- NovedadApartamento : novedades
Folio *-- AutorizacionCierre
Alojamiento *-- ServicioAdicional : servicios
Alojamiento *-- Ubicacion
NocheCalendario *-- EstadoNoche
Reserva ..> Apartamento : IdentificacionApartamento
Reserva ..> PoliticaCancelacion : VersionPolitica
Folio ..> Reserva : CodigoReserva
Bloqueo ..> Apartamento : IdentificacionApartamento
```

## Servicios de dominio y puertos

```mermaid
classDiagram
direction LR
  class DisponibilidadApartamentoService {
    +verificarDisponible(apto, estancia, total, excluida, hoy) void
    +estaDisponible(...) boolean
    +calendario(Apartamento, Estancia) List~NocheCalendario~
  }
  class CotizadorEstanciaService {
    +cotizar(...) ValorCongelado
  }
  class EstanciaMinimaTemporadaService {
    +verificar(ValorCongelado) void
  }
  class RetencionCancelacionService {
    +calcular(...) Dinero
    +calcularNoShow(Reserva) Dinero
  }
  class EntregaApartamentoService {
    +autorizarEntrega(...) Apartamento
  }
  class SalidaApartamentoService {
    +verificar(...) void
  }
  class AnticipoReservaService {
    +verificar(Reserva, Folio) void
  }
  class ModificacionReservaService {
    +validarModificacion(...) ValorCongelado
  }
  class ConciliacionCanalExternoService {
    +verificarNoDuplicada(...) void
  }
  class DisponibilidadParqueaderoService {
    +verificarVehiculoAdicional(Reserva) Dinero
  }
  class BloqueoApartamentoService {
    +validarBloqueo(...) void
  }
  class RetiroApartamentoService {
    +validarRetiro(Apartamento) void
  }
  class RegistroApartamentoService {
    +verificarIdentificacionDisponible(...) void
  }
  class TitularReservasService {
    +verificarSinReservasActivas(DocumentoIdentidad) void
  }
  class ReservaRepository {
    <<interface>>
    +obtenerPorCodigo()
    +guardar()
    +buscarPorEstado()
    +buscarActivasPorApartamento()
    +buscar(FiltroReservas, PeticionPagina) Pagina
    +existePorCanalEIdentificadorExterno()
    +existenActivasDelTitular()
    +buscarLlegadasPrevistas()
    +buscarSalidasPrevistas()
    +buscarActivasEnPeriodo()
  }
  class ApartamentoRepository {
    <<interface>>
    +obtenerPorIdentificacion()
    +guardar()
    +listar(estado, incluirRetirados, pagina)
    +buscarActivosConCapacidadPara()
    +existe()
  }
  class FolioRepository {
    <<interface>>
    +obtenerPorReserva()
    +guardar()
  }
  class BloqueoRepository {
    <<interface>>
    +buscarVigentesPorApartamento()
    +obtenerPorIdentificacion()
    +listarPorApartamento()
    +guardar()
  }
  class PoliticaCancelacionRepository {
    <<interface>>
    +obtenerVigente()
    +obtenerPorVersion()
    +publicar()
  }
  class TemporadaRepository {
    <<interface>>
    +obtenerCalendario()
    +listar()
    +guardar()
  }
  class AlojamientoRepository {
    <<interface>>
    +obtener() Optional
    +guardar()
  }
  class TarifarioRepository {
    <<interface>>
    +tarifaDe(...) Tarifa
  }
  class GeneradorCodigoReserva {
    <<interface>>
    +generar(FechaCreacion) CodigoReserva
  }
  class ConfiguracionAlojamiento {
    <<interface>>
  }
DisponibilidadApartamentoService ..> ReservaRepository
DisponibilidadApartamentoService ..> BloqueoRepository
DisponibilidadApartamentoService ..> ConfiguracionAlojamiento
CotizadorEstanciaService ..> TarifarioRepository
CotizadorEstanciaService ..> ConfiguracionAlojamiento
EstanciaMinimaTemporadaService ..> ConfiguracionAlojamiento
RetencionCancelacionService ..> PoliticaCancelacionRepository
EntregaApartamentoService ..> ApartamentoRepository
AnticipoReservaService ..> ConfiguracionAlojamiento
ModificacionReservaService ..> DisponibilidadApartamentoService
ModificacionReservaService ..> CotizadorEstanciaService
ModificacionReservaService ..> EstanciaMinimaTemporadaService
ConciliacionCanalExternoService ..> ReservaRepository
DisponibilidadParqueaderoService ..> ReservaRepository
DisponibilidadParqueaderoService ..> ConfiguracionAlojamiento
BloqueoApartamentoService ..> ReservaRepository
BloqueoApartamentoService ..> BloqueoRepository
RetiroApartamentoService ..> ReservaRepository
RegistroApartamentoService ..> ApartamentoRepository
TitularReservasService ..> ReservaRepository
```

## Acceso y seguridad (capa de aplicación, 3.6)

El dominio no conoce al usuario: el vínculo con el negocio es el documento del titular (`Reserva.perteneceA`).

```mermaid
classDiagram
direction LR
  class Usuario {
    -UUID identificacion
    -CorreoElectronico correo
    -DocumentoIdentidad documento
    -LocalDate fechaNacimiento
    -String nombreCompleto
    -String telefono
    -Rol rol
    -String contrasenaCifrada
    -boolean activo
    +registrarHuesped(...)$ Usuario
    +crearConRol(...)$ Usuario
    +actualizarPerfil(String, String) void
    +cambiarContrasena(String) void
    +asignarRol(Rol) void
    +desactivar() void
  }
  class Rol {
    <<enumeration>>
    ADMINISTRADOR
    RECEPCIONISTA
    PERSONAL_SERVICIO
    HUESPED
    +gestionaReservas() boolean
  }
  class CorreoElectronico {
    -String valor
  }
  class CodigoRecuperacion {
    -CorreoElectronico correo
    -String codigoCifrado
    -LocalDateTime expiraEn
    -boolean usado
    +estaVigente(LocalDateTime) boolean
    +marcarUsado() void
  }
  class Solicitante {
    -String usuario
    -Rol rol
    -DocumentoIdentidad documento
    +verificarAcceso(Reserva) void
    +verificarPuedeReservar(CanalOrigen, DocumentoIdentidad) void
  }
  class PoliticaContrasena {
    +verificar(String)$ void
  }
  class UsuarioRepository {
    <<interface>>
  }
  class CodigoRecuperacionRepository {
    <<interface>>
  }
  class NotificadorCorreo {
    <<interface>>
    +enviarCodigoRecuperacion(...) void
  }
Usuario *-- CorreoElectronico
Usuario *-- Rol
CodigoRecuperacion *-- CorreoElectronico
Solicitante *-- Rol
UsuarioRepository ..> Usuario
CodigoRecuperacionRepository ..> CodigoRecuperacion
```

## Adaptadores de infraestructura

| Puerto | Adaptador que usa la aplicación | Adaptador en memoria (solo pruebas) |
|---|---|---|
| `ReservaRepository` | `ReservaJpaRepository` | `ReservaRepositoryEnMemoria` |
| `ApartamentoRepository` | `ApartamentoJpaRepository` | `ApartamentoRepositoryEnMemoria` |
| `FolioRepository` | `FolioJpaRepository` | `FolioRepositoryEnMemoria` |
| `BloqueoRepository` | `BloqueoJpaRepository` | `BloqueoRepositoryEnMemoria` |
| `PoliticaCancelacionRepository` | `PoliticaCancelacionJpaRepository` | `PoliticaCancelacionRepositoryEnMemoria` |
| `TemporadaRepository` | `TemporadaJpaRepository` | `TemporadaRepositoryEnMemoria` |
| `AlojamientoRepository` | `AlojamientoJpaRepository` | `AlojamientoRepositoryEnMemoria` |
| `UsuarioRepository` (aplicación) | `UsuarioJpaRepository` | `UsuarioRepositoryEnMemoria` |
| `CodigoRecuperacionRepository` (aplicación) | `CodigoRecuperacionJpaRepository` | `CodigoRecuperacionRepositoryEnMemoria` |
| `NotificadorCorreo` (aplicación) | `NotificadorCorreoLocal` (I-02: escribe en el log) | mock |
| `ConfiguracionSeguridad` (aplicación) | `ConfiguracionSeguridadPropiedades` | mock |
| `GeneradorCodigoReserva` | `GeneradorCodigoReservaJpa` | `GeneradorCodigoReservaEnMemoria` |
| `TarifarioRepository` | `TarifarioRepositoryAdapter` (temporada por `CalendarioTemporadas`, tarifa por `Apartamento`) | lambda en las pruebas |
| `ConfiguracionAlojamiento` | `ParametrosAlojamiento` creado desde `PropiedadesAlojamiento` | `ParametrosDePrueba` |
| `CifradorContrasenas` (aplicación) | `CifradorContrasenasBCrypt` | mock |
| `EmisorTokens` (aplicación) | `EmisorTokensJwt` | mock |
