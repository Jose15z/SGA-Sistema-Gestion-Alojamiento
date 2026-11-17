import { useEffect, useRef, type ReactNode } from 'react';
import { Icono } from './Icono';

interface DialogoConfirmacionProps {
  titulo: string;
  children: ReactNode;
  textoConfirmar: string;
  peligroso?: boolean;
  ocupado?: boolean;
  // Deshabilita solo el botón de confirmar (por ejemplo, mientras falta el motivo).
  confirmarDeshabilitado?: boolean;
  onConfirmar: () => void;
  onCancelar: () => void;
}

// Pide confirmación antes de una acción que no se puede deshacer (no-show, cancelar, cerrar folio).
// Usa <dialog> nativo: atrapa el foco, se cierra con Esc y queda accesible para el lector de pantalla.
export function DialogoConfirmacion({
  titulo,
  children,
  textoConfirmar,
  peligroso = false,
  ocupado = false,
  confirmarDeshabilitado = false,
  onConfirmar,
  onCancelar,
}: DialogoConfirmacionProps) {
  const dialogo = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    dialogo.current?.showModal();
  }, []);

  return (
    <dialog ref={dialogo} className="dialogo" onCancel={onCancelar} aria-labelledby="dialogo-titulo">
      <div className={`dialogo-icono ${peligroso ? 'peligro' : ''}`}>
        <Icono nombre={peligroso ? 'alerta' : 'chequeo'} tamano={22} />
      </div>
      <h3 id="dialogo-titulo">{titulo}</h3>
      <div className="dialogo-contenido">{children}</div>
      <div className="button-row">
        <button type="button" className="secondary" onClick={onCancelar} disabled={ocupado}>
          Volver
        </button>
        <button
          type="button"
          className={peligroso ? 'primary peligro' : 'primary'}
          onClick={onConfirmar}
          disabled={ocupado || confirmarDeshabilitado}
        >
          {ocupado ? 'Procesando…' : textoConfirmar}
        </button>
      </div>
    </dialog>
  );
}
