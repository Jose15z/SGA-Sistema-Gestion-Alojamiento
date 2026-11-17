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
    { ruta: '/administracion/usuarios', etiqueta: 'Usuarios', icono: 'usuarios' },
    { ruta: '/recepcion', etiqueta: 'Operación diaria', icono: 'calendario' },
    { ruta: '/recepcion/nueva-reserva', etiqueta: 'Nueva reserva', icono: 'mas' },
    { ruta: '/reportes', etiqueta: 'Reportes', icono: 'grafica' },
    { ruta: '/preparacion', etiqueta: 'Estado operativo', icono: 'destello' },
  ],
};

// Secciones a las que puede entrar cada rol (con todas sus subrutas).
// Es la misma matriz de roles del backend (SeguridadConfig): el frontend solo
// evita mostrar pantallas que el servidor de todos modos rechazaría.
const seccionesPorRol: Record<Rol, string[]> = {
  HUESPED: ['/apartamentos', '/reservas', '/mis-reservas'],
  RECEPCIONISTA: ['/recepcion', '/apartamentos'],
  PERSONAL_SERVICIO: ['/preparacion'],
  ADMINISTRADOR: ['/administracion', '/recepcion', '/reportes', '/preparacion', '/apartamentos'],
};

function estaEnSeccion(ruta: string, seccion: string): boolean {
  return ruta === seccion || ruta.startsWith(`${seccion}/`);
}

// Pantalla de inicio de cada rol: la primera opción de su menú.
export function inicioDeRol(rol: Rol): string {
  return navegacionPorRol[rol][0].ruta;
}

// Una ruta que no es de ninguna sección (por ejemplo, una dirección mal escrita)
// se deja pasar para que la atienda "Página no encontrada".
export function rutaPermitida(rol: Rol, ruta: string): boolean {
  const todas = Object.values(seccionesPorRol).flat();
  if (!todas.some((seccion) => estaEnSeccion(ruta, seccion))) {
    return true;
  }
  return seccionesPorRol[rol].some((seccion) => estaEnSeccion(ruta, seccion));
}

// Título de la barra superior según la sección en la que está el usuario.
const titulosPorSeccion: [string, string][] = [
  ['/apartamentos', 'Disponibilidad'],
  ['/reservas', 'Reservas'],
  ['/mis-reservas', 'Mis reservas'],
  ['/recepcion/nueva-reserva', 'Nueva reserva'],
  ['/recepcion/reservas', 'Reservas y folios'],
  ['/recepcion', 'Recepción del día'],
  ['/preparacion', 'Preparación'],
  ['/administracion/usuarios', 'Usuarios'],
  ['/administracion', 'Administración'],
  ['/reportes', 'Reportes'],
];

export function tituloDeSeccion(ruta: string): string {
  const seccion = titulosPorSeccion.find(([prefijo]) => estaEnSeccion(ruta, prefijo));
  return seccion ? seccion[1] : 'Siete Raíces';
}
