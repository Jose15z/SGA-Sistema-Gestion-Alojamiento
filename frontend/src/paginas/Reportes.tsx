import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Reportes() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="ADMINISTRACIÓN"
        titulo="Reportes"
        descripcion="Ocupación, ingresos y reservas por canal."
      />
      <PantallaPendiente mockup="15-reportes.png" />
    </section>
  );
}
