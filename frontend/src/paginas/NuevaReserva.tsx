import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function NuevaReserva() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="RESERVA EN LÍNEA"
        titulo="Reservar apartamento"
        descripcion="El huésped crea su reserva por el portal: fechas, ocupantes y hora estimada de llegada."
      />
      <PantallaPendiente mockup="04-detalle-apartamento.png" />
    </section>
  );
}
