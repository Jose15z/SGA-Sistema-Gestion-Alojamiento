import type { ReactNode } from 'react';
import { Icono, type NombreIcono } from './Icono';

interface CampoProps {
  id: string;
  etiqueta: string;
  icono?: NombreIcono;
  // Mensaje de error del campo: se anuncia al lector de pantalla y se enlaza con aria-describedby.
  error?: string;
  className?: string;
  // Un botón dentro del campo, como "Ver" en la contraseña.
  accion?: ReactNode;
  children: ReactNode;
}

export function Campo({ id, etiqueta, icono, error, className, accion, children }: CampoProps) {
  return (
    <label htmlFor={id} className={className}>
      {etiqueta}
      <span className="auth-input">
        {icono && <Icono nombre={icono} tamano={18} />}
        {children}
        {accion}
      </span>
      {error && (
        <span className="error" id={`${id}-error`} role="alert">
          {error}
        </span>
      )}
    </label>
  );
}
