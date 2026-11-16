import type { ApartamentoResumen } from '../modelos/apartamento';

// Datos de ejemplo mientras el frontend no consulta el backend.
// Son los siete apartamentos de la Ficha del Alojamiento de Siete Raíces (Anexo A, A.2).
export const apartamentosEjemplo: ApartamentoResumen[] = [
  { identificacion: 'APT-101', nombre: 'Café', dormitorios: 1, capacidad: 2 },
  { identificacion: 'APT-102', nombre: 'Guadua', dormitorios: 1, capacidad: 3 },
  { identificacion: 'APT-201', nombre: 'Palma', dormitorios: 2, capacidad: 4 },
  { identificacion: 'APT-202', nombre: 'Yarumo', dormitorios: 2, capacidad: 5 },
  { identificacion: 'APT-301', nombre: 'Orquídea', dormitorios: 2, capacidad: 4 },
  { identificacion: 'APT-302', nombre: 'Barranquero', dormitorios: 3, capacidad: 6 },
  { identificacion: 'APT-303', nombre: 'Cocora', dormitorios: 3, capacidad: 6 },
];
