import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Recepcion() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="OPERACIÓN DIARIA"
        titulo="Recepción del día"
        descripcion="Llegadas y salidas previstas: registro de llegada (check-in), salida (check-out) y no-show."
      />
      <PantallaPendiente mockup="06-recepcion-del-dia.png" />
    </section>
  );
}
