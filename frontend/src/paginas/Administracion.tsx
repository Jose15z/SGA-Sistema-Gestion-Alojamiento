import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Administracion() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="ADMINISTRACIÓN"
        titulo="Administración"
        descripcion="Apartamentos, temporadas y tarifas, política de cancelación, bloqueos y usuarios."
      />
      <PantallaPendiente mockup="10-admin-apartamentos.png" />
    </section>
  );
}
