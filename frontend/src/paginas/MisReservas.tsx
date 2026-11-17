import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function MisReservas() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="MI CUENTA"
        titulo="Mis reservas"
        descripcion="El huésped consulta las reservas en las que es titular y el folio de cada una."
      />
      <PantallaPendiente mockup="05-mis-reservas.png" />
    </section>
  );
}
