import type { ReactNode } from 'react';

interface EncabezadoPaginaProps {
  antetitulo: string;
  titulo: string;
  descripcion: string;
  // Botones o enlaces que van a la derecha del título.
  children?: ReactNode;
}

export function EncabezadoPagina({ antetitulo, titulo, descripcion, children }: EncabezadoPaginaProps) {
  return (
    <div className="page-intro">
      <div>
        <span className="eyebrow">{antetitulo}</span>
        <h1>{titulo}</h1>
        <p>{descripcion}</p>
      </div>
      {children}
    </div>
  );
}
