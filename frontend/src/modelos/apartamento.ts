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
