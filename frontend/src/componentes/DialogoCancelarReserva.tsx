import { useState } from 'react';
import { nombreEstado, type EstadoReserva } from '../modelos/reserva';
import { DialogoConfirmacion } from './DialogoConfirmacion';

interface DialogoCancelarReservaProps {
  codigo: string;
  estado: EstadoReserva;
  versionPolitica: number;
  ocupado: boolean;
  onConfirmar: (motivo: string) => void;
  onCancelar: () => void;
}

/** Modal de confirmación para cancelar una reserva del huésped según su política congelada. */
export function DialogoCancelarReserva({
  codigo,
  estado,
  versionPolitica,
  ocupado,
  onConfirmar,
  onCancelar,
}: DialogoCancelarReservaProps) {
  const [motivo, setMotivo] = useState('');

  return (
    <DialogoConfirmacion
      titulo={`Cancelar la reserva ${codigo}`}
      textoConfirmar="Cancelar reserva"
      peligroso
      ocupado={ocupado}
      confirmarDeshabilitado={!motivo.trim()}
      onConfirmar={() => {
        if (!motivo.trim()) return;
        onConfirmar(motivo.trim());
      }}
      onCancelar={onCancelar}
    >
      <p>
        Reserva <strong>{codigo}</strong> (estado actual: <strong>{nombreEstado[estado]}</strong>). Se aplicará la
        política de cancelación versión <strong>{versionPolitica}</strong> congelada al crear la reserva, lo que puede
        generar una penalidad según la antelación. Esta acción no se puede deshacer.
      </p>
      <label htmlFor={`motivo-cancelacion-${codigo}`}>
        MOTIVO DE LA CANCELACIÓN
        <textarea
          id={`motivo-cancelacion-${codigo}`}
          rows={3}
          maxLength={300}
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
          placeholder="Ej. Cambio en mis fechas de viaje"
        />
      </label>
    </DialogoConfirmacion>
  );
}
