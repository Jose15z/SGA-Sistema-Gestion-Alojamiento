import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Folio() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="OPERACIÓN FINANCIERA"
        titulo="Folio de la reserva"
        descripcion="Cargos, pagos y saldo del folio; registro de pagos y cierre."
      />
      <PantallaPendiente mockup="08-reservas-y-folios.png" />
    </section>
  );
}
