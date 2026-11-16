import type { ReactNode } from 'react';

// Tonos definidos en index.css (.badge.*).
export type TonoEtiqueta = 'neutral' | 'light' | 'available' | 'confirmed' | 'warning' | 'danger';

interface EtiquetaProps {
  tono?: TonoEtiqueta;
  children: ReactNode;
}

export function Etiqueta({ tono = 'neutral', children }: EtiquetaProps) {
  return <span className={`badge ${tono}`}>{children}</span>;
}
