import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function NuevaReservaDirecta() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="CANAL DIRECTO"
        titulo="Nueva reserva"
        descripcion="La recepción crea una reserva para un titular que puede no tener cuenta."
      />
      <PantallaPendiente mockup="07-nueva-reserva.png" />
    </section>
  );
}
