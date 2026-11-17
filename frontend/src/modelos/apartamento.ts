// Vista breve de un apartamento, tal como la mostrará el frontend.
// En la Guía 15 este tipo se alinea con el DTO que expone el backend.
export interface ApartamentoResumen {
  identificacion: string;   // "APT-301": la identidad del negocio (L-04)
  nombre: string;
  dormitorios: number;
  capacidad: number;        // tope rígido de personas (F-03)
}

export type EstadoOperativo = 'PREPARADO' | 'OCUPADO' | 'PENDIENTE_PREPARACION' | 'EN_PREPARACION' | 'FUERA_DE_SERVICIO';

// Espejo de: record ApartamentoResponse(...) — lo consultan la recepción y la administración
export interface Apartamento {
  identificacion: string;
  nombre: string;
  descripcion: string;
  dormitorios: number;
  capacidad: number;
  caracteristicas: string[];
  estadoOperativo: EstadoOperativo;
  activo: boolean;
  tarifas: { temporada: 'BASE' | 'ALTA' | 'ESPECIAL'; valorPorOcupanteNoche: number }[];
}

export type UnidadCobro = 'POR_SOLICITUD' | 'POR_PERSONA' | 'POR_NOCHE';

// Espejo de: record ServicioAdicionalResponse(...)
export interface ServicioAdicional {
  codigo: string;
  nombre: string;
  generaCargo: boolean;
  valor: number;
  unidad: UnidadCobro;
}

// Espejo de: record AlojamientoResponse(...) — GET /api/alojamiento
export interface Alojamiento {
  nombre: string;
  descripcion: string;
  ciudad: string;
  direccion: string;
  latitud: number;
  longitud: number;
  horaEntrada: string;
  horaSalida: string;
  normas: string[];
  servicios: ServicioAdicional[];
}

// Espejo de: record TramoCancelacionResponse(...)
export interface TramoCancelacion {
  antelacionMinimaHoras: number;
  porcentaje: number;
}

// Espejo de: record PoliticaCancelacionResponse(...) — GET /api/politicas-cancelacion/vigente
export interface PoliticaCancelacion {
  version: number;
  tramos: TramoCancelacion[];
  porcentajeNoShow: number;
  horaEntrada: string;
}

// Espejo de: record ApartamentoDisponibleResponse(...) — GET /api/apartamentos/disponibles
export interface ApartamentoDisponible {
  identificacion: string;
  nombre: string;
  descripcion: string;
  dormitorios: number;
  capacidadMaxima: number;
  caracteristicas: string[];
  noches: number;
  valorEstimado: number;
}

// Parámetros de consulta de GET /api/apartamentos/disponibles
export interface FiltroDisponibles {
  fechaEntrada: string;
  fechaSalida: string;
  totalOcupantes: number;
  fechasNacimiento?: string[];
  capacidadMinima?: number;
  dormitoriosMinimos?: number;
  precioMinimo?: number;
  precioMaximo?: number;
  caracteristicas?: string[];
  page?: number;
  size?: number;
}

export const nombreUnidadCobro: Record<UnidadCobro, string> = {
  POR_SOLICITUD: 'Por solicitud',
  POR_PERSONA: 'Por persona',
  POR_NOCHE: 'Por noche',
};
