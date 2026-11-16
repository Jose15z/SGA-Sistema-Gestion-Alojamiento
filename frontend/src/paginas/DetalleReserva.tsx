import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function DetalleReserva() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="MI CUENTA"
        titulo="Detalle de la reserva"
        descripcion="Estancia, ocupantes, folio y cancelación de una reserva del huésped."
      />
      <PantallaPendiente mockup="05-mis-reservas.png" />
    </section>
  );
}
