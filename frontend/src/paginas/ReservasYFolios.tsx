import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function ReservasYFolios() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="OPERACIÓN FINANCIERA"
        titulo="Reservas y folios"
        descripcion="Listado de reservas con filtros por estado, canal y apartamento."
      />
      <PantallaPendiente mockup="08-reservas-y-folios.png" />
    </section>
  );
}
