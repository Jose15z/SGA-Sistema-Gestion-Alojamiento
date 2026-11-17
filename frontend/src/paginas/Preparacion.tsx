import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Preparacion() {
  return (
    <section>
      <EncabezadoPagina
        antetitulo="PERSONAL DE SERVICIO"
        titulo="Preparación de apartamentos"
        descripcion="Pendiente de preparación, en preparación y preparado; registro de novedades."
      />
      <PantallaPendiente mockup="09-preparacion.png" />
    </section>
  );
}
