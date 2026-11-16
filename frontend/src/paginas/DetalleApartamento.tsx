import { Link, useParams } from 'react-router';
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
      <h1>{apartamento.nombre}</h1>
      <p>Identificación: {apartamento.identificacion}</p>
      <p>Dormitorios: {apartamento.dormitorios}</p>
      <p>Capacidad: {apartamento.capacidad} personas</p>
      <Link to="/">Volver</Link>
    </section>
  );
}
