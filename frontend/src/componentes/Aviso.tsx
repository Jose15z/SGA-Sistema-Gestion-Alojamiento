import { useEffect } from 'react';
import { Icono } from './Icono';

interface AvisoProps {
  mensaje: string;
  onCerrar: () => void;
}

// Confirmación breve de una acción exitosa (abajo a la derecha). Se oculta sola a los 4 segundos.
export function Aviso({ mensaje, onCerrar }: AvisoProps) {
  useEffect(() => {
    const temporizador = window.setTimeout(onCerrar, 4000);
    return () => window.clearTimeout(temporizador);
  }, [mensaje, onCerrar]);

  return (
    <div className="toast" role="status">
      <span>
        <Icono nombre="chequeo" tamano={14} />
      </span>
      {mensaje}
      <button type="button" className="toast-cerrar" onClick={onCerrar} aria-label="Cerrar aviso">
        <Icono nombre="cerrar" tamano={14} />
      </button>
    </div>
  );
}
