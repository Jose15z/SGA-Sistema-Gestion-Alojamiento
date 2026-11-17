import { nombreEstado, type EstadoReserva } from '../modelos/reserva';
import { Etiqueta, type TonoEtiqueta } from './Etiqueta';

const tonoPorEstado: Record<EstadoReserva, TonoEtiqueta> = {
  PENDIENTE: 'warning',
  CONFIRMADA: 'confirmed',
  EN_CURSO: 'available',
  FINALIZADA: 'neutral',
  CANCELADA: 'danger',
  NO_SHOW: 'danger',
};


export function EtiquetaEstado({ estado }: { estado: EstadoReserva }) {
  return (
    <Etiqueta tono={tonoPorEstado[estado]}>
      <i /> {nombreEstado[estado]}
    </Etiqueta>
  );
}
