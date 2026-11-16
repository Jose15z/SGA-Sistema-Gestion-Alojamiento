import type { NombreIcono } from '../componentes/Icono';
import type { Rol } from '../modelos/rol';

export interface OpcionNavegacion {
  ruta: string;
  etiqueta: string;
  icono: NombreIcono;
}

// Menú de la barra lateral para cada rol, en el orden del diseño de Figma.
export const navegacionPorRol: Record<Rol, OpcionNavegacion[]> = {
  HUESPED: [
    { ruta: '/apartamentos', etiqueta: 'Buscar apartamentos', icono: 'buscar' },
    { ruta: '/mis-reservas', etiqueta: 'Mis reservas', icono: 'calendario' },
  ],
  RECEPCIONISTA: [
    { ruta: '/recepcion', etiqueta: 'Recepción del día', icono: 'calendario' },
    { ruta: '/recepcion/nueva-reserva', etiqueta: 'Nueva reserva', icono: 'mas' },
    { ruta: '/recepcion/reservas', etiqueta: 'Reservas y folios', icono: 'billetera' },
    { ruta: '/apartamentos', etiqueta: 'Disponibilidad', icono: 'buscar' },
  ],
  PERSONAL_SERVICIO: [{ ruta: '/preparacion', etiqueta: 'Preparación', icono: 'destello' }],
  ADMINISTRADOR: [
    { ruta: '/administracion', etiqueta: 'Administración', icono: 'edificio' },
    { ruta: '/recepcion', etiqueta: 'Operación diaria', icono: 'calendario' },
    { ruta: '/recepcion/nueva-reserva', etiqueta: 'Nueva reserva', icono: 'mas' },
    { ruta: '/reportes', etiqueta: 'Reportes', icono: 'grafica' },
    { ruta: '/preparacion', etiqueta: 'Estado operativo', icono: 'destello' },
  ],
};

// Título de la barra superior según la sección en la que está el usuario.
const titulosPorSeccion: [string, string][] = [
  ['/apartamentos', 'Disponibilidad'],
  ['/reservas', 'Reservas'],
  ['/mis-reservas', 'Mis reservas'],
  ['/recepcion/nueva-reserva', 'Nueva reserva'],
  ['/recepcion/reservas', 'Reservas y folios'],
  ['/recepcion', 'Recepción del día'],
  ['/preparacion', 'Preparación'],
  ['/administracion', 'Administración'],
  ['/reportes', 'Reportes'],
];

export function tituloDeSeccion(ruta: string): string {
  const seccion = titulosPorSeccion.find(([prefijo]) => ruta.startsWith(prefijo));
  return seccion ? seccion[1] : 'Siete Raíces';
}

// Rol con el que se abre la barra lateral cuando se entra directo a una ruta.
export function rolDeRuta(ruta: string): Rol {
  const roles = Object.keys(navegacionPorRol) as Rol[];
  const rol = roles.find((r) => navegacionPorRol[r].some((opcion) => ruta.startsWith(opcion.ruta)));
  return rol ?? 'HUESPED';
}
