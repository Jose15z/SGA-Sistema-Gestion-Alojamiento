// Enums cerrados del dominio: viajan como texto y se tipan como uniones.
export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'EN_CURSO' | 'FINALIZADA' | 'CANCELADA' | 'NO_SHOW';

export type CanalOrigen = 'PORTAL' | 'DIRECTO' | 'EXTERNO';

export const nombreEstado: Record<EstadoReserva, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  NO_SHOW: 'No-show',
};

export const nombreCanal: Record<CanalOrigen, string> = {
  PORTAL: 'Portal web',
  DIRECTO: 'Directo (recepción)',
  EXTERNO: 'Canal externo',
};

// Espejo de: record OcupanteRequest(String documento, String nombreCompleto, LocalDate fechaNacimiento)
export interface OcupanteRequest {
  documento: string;
  nombreCompleto: string;
  fechaNacimiento: string;   // LocalDate viaja como "AAAA-MM-DD"
}

// Espejo de: record CrearReservaRequest(...)
export interface CrearReservaRequest {
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  canalOrigen: CanalOrigen;
  identificadorExterno: string | null;   // solo para el canal EXTERNO
  titular: OcupanteRequest;
  acompanantes: OcupanteRequest[];       // el titular NO va aquí
  horaEstimadaLlegada: string | null;    // LocalTime: "16:30"
  mascotas: number | null;
}

// Espejo de: record OcupanteResponse(...)
export interface OcupanteResponse {
  documento: string;
  nombreCompleto: string;
  fechaNacimiento: string;
}

// Espejo de: record CargoNocheResponse(...)
export interface CargoNoche {
  noche: string;
  temporada: 'BASE' | 'ALTA' | 'ESPECIAL';
  tarifaOcupanteNoche: number;
  ocupantesFacturables: number;
  subtotalNoche: number;
}

// Espejo de: record CotizacionResponse(...)
export interface Cotizacion {
  subtotal: number;
  porcentajeDescuento: number;
  valorDescuento: number;
  valorTotal: number;
  totalNoches: number;
  detalleNoches: CargoNoche[];
}

// Espejo de: record CotizacionRequest(...)
export interface CotizacionRequest {
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  ocupantes: { fechaNacimiento: string }[];
}

// Espejo de: record EventoHistorialResponse(...)
export interface EventoHistorial {
  momento: string;
  accion: string;
  autor: string;
  estadoAnterior: EstadoReserva | null;
  estadoNuevo: EstadoReserva | null;
  observacion: string | null;
}

// Espejo de: record ReservaDetalleResponse(...)
export interface ReservaDetalle {
  codigo: string;
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  canalOrigen: CanalOrigen;
  identificadorExterno: string | null;
  estado: EstadoReserva;
  fechaCreacion: string;
  horaEstimadaLlegada: string | null;
  novedadLlegada: string | null;
  mascotas: number;
  vehiculos: string[];
  titular: OcupanteResponse;
  ocupantes: OcupanteResponse[];
  cotizacionCongelada: Cotizacion;
  versionPolitica: number;
  historial: EventoHistorial[];
}

// Espejo de: record ReservaResumenResponse(...)
export interface ReservaResumen {
  codigo: string;
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  estado: EstadoReserva;
  canalOrigen: CanalOrigen;
  titularNombre: string;
  valorTotal: number;
  fechaCreacion: string;
}

// Espejo de: record MovimientosDelDiaResponse(...)
export interface MovimientosDelDia {
  dia: string;
  llegadas: ReservaResumen[];
  salidas: ReservaResumen[];
}

// Espejo de: record PaginaResponse<T>(...)
export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
}

// Filtros de GET /api/reservas
export interface FiltroReservas {
  estado?: EstadoReserva;
  canal?: CanalOrigen;
  apartamento?: string;
  titular?: string;
  page?: number;
  size?: number;
}

// Acciones que el dominio registra en el historial (Reserva.java), en lenguaje para el usuario.
export const nombreAccionHistorial: Record<string, string> = {
  CREACION: 'Reserva creada',
  HORA_LLEGADA: 'Hora de llegada registrada',
  CONFIRMACION: 'Reserva confirmada',
  REGISTRO: 'Llegada registrada (check-in)',
  SALIDA: 'Salida registrada (check-out)',
  CANCELACION: 'Reserva cancelada',
  NO_SHOW: 'No-show declarado',
  VENCIMIENTO: 'Reserva vencida por falta de anticipo',
  MODIFICACION: 'Estancia modificada',
  NOVEDAD_LLEGADA: 'Novedad de llegada informada',
  RESOLUCION_NOVEDAD: 'Novedad de llegada resuelta',
  MASCOTAS: 'Mascotas registradas',
  VEHICULO: 'Vehículo registrado',
};
