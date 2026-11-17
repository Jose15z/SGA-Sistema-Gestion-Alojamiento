import { Link, useParams } from 'react-router';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';

export default function DetalleApartamento() {
  // El nombre del parámetro coincide con el de la ruta: /apartamentos/:identificacion
  const { identificacion } = useParams();
  const apartamento = apartamentosEjemplo.find((a) => a.identificacion === identificacion);

  if (!apartamento) {
    return <p>No existe el apartamento {identificacion}.</p>;
  }

  return (
    <section>
      <EncabezadoPagina
        antetitulo={`${apartamento.identificacion} · APARTAMENTO COMPLETO`}
        titulo={apartamento.nombre}
        descripcion="Detalle, calendario y cotización. Diseño de referencia: mockups/pantallas/04-detalle-apartamento.png"
      />
      <p>Dormitorios: {apartamento.dormitorios}</p>
      <p>Capacidad: {apartamento.capacidad} personas</p>
      <Link to="/apartamentos">Volver</Link>
    </section>
  );
}
