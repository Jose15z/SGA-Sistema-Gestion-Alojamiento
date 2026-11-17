import type { ReactNode } from 'react';

export type NombreIcono =
  | 'hoja'
  | 'buscar'
  | 'calendario'
  | 'cama'
  | 'usuarios'
  | 'billetera'
  | 'destello'
  | 'edificio'
  | 'grafica'
  | 'ajustes'
  | 'salir'
  | 'campana'
  | 'mas'
  | 'flecha'
  | 'chequeo'
  | 'reloj'
  | 'mapa'
  | 'filtro'
  | 'cerrar'
  | 'desplegar'
  | 'escudo'
  | 'tarjeta'
  | 'luna'
  | 'llave'
  | 'alerta'
  | 'menu';

// Íconos de línea del diseño de Figma (24 × 24, trazo de 1.8).
const trazos: Record<NombreIcono, ReactNode> = {
  hoja: (
    <>
      <path d="M20 4C13 4 6 7 5 14c3 1 7 0 10-3" />
      <path d="M4 20c2-6 6-9 12-12" />
    </>
  ),
  buscar: (
    <>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-4-4" />
    </>
  ),
  calendario: (
    <>
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M16 3v4M8 3v4M3 10h18" />
    </>
  ),
  cama: (
    <>
      <path d="M3 19v-8h18v8M3 15h18M7 11V7h5a3 3 0 0 1 3 3v1" />
    </>
  ),
  usuarios: (
    <>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
    </>
  ),
  billetera: (
    <>
      <path d="M20 7V5a2 2 0 0 0-2-2H5a3 3 0 0 0 0 6h15v12H5a3 3 0 0 1-3-3V6" />
      <path d="M16 15h2" />
    </>
  ),
  destello: (
    <>
      <path d="m12 3-1 4-4 1 4 1 1 4 1-4 4-1-4-1-1-4Z" />
      <path d="m19 14-.7 2.3L16 17l2.3.7L19 20l.7-2.3L22 17l-2.3-.7L19 14Z" />
    </>
  ),
  edificio: (
    <>
      <path d="M4 21V3h11v18M15 9h5v12M8 7h3M8 11h3M8 15h3M2 21h20" />
    </>
  ),
  grafica: (
    <>
      <path d="M4 19V9M10 19V5M16 19v-7M22 19H2" />
    </>
  ),
  ajustes: (
    <>
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H2.8v-4H3a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3A1.7 1.7 0 0 0 10 3V2.8h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z" />
    </>
  ),
  salir: (
    <>
      <path d="M10 17l5-5-5-5M15 12H3M21 19V5a2 2 0 0 0-2-2h-6" />
    </>
  ),
  campana: (
    <>
      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" />
    </>
  ),
  mas: <path d="M12 5v14M5 12h14" />,
  flecha: <path d="m9 18 6-6-6-6" />,
  chequeo: <path d="m5 12 4 4L19 6" />,
  reloj: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </>
  ),
  mapa: (
    <>
      <path d="m3 6 6-3 6 3 6-3v15l-6 3-6-3-6 3V6Z" />
      <path d="M9 3v15M15 6v15" />
    </>
  ),
  filtro: <path d="M4 5h16M7 12h10M10 19h4" />,
  cerrar: <path d="m6 6 12 12M18 6 6 18" />,
  desplegar: <path d="m8 10 4 4 4-4" />,
  escudo: (
    <>
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z" />
      <path d="m9 12 2 2 4-4" />
    </>
  ),
  tarjeta: (
    <>
      <rect x="2" y="5" width="20" height="14" rx="2" />
      <path d="M2 10h20M6 15h3" />
    </>
  ),
  luna: <path d="M20 15.5A8.5 8.5 0 0 1 8.5 4 8.5 8.5 0 1 0 20 15.5Z" />,
  llave: (
    <>
      <circle cx="8" cy="15" r="4" />
      <path d="m11 12 9-9M17 6l3 3M14 9l3 3" />
    </>
  ),
  alerta: (
    <>
      <path d="M10.3 3.6 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.6a2 2 0 0 0-3.4 0Z" />
      <path d="M12 9v4M12 17h.01" />
    </>
  ),
  menu: <path d="M4 7h16M4 12h16M4 17h16" />,
};

interface IconoProps {
  nombre: NombreIcono;
  tamano?: number;
}

export function Icono({ nombre, tamano = 20 }: IconoProps) {
  return (
    <svg
      width={tamano}
      height={tamano}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {trazos[nombre]}
    </svg>
  );
}
