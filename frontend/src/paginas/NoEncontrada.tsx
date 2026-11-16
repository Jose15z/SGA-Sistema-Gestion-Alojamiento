import { Link } from 'react-router';
import { Icono } from '../componentes/Icono';

export default function NoEncontrada() {
  return (
    <div className="empty">
      <Icono nombre="alerta" tamano={28} />
      <h3>Página no encontrada</h3>
      <p>
        <Link to="/">Ir al inicio</Link>
      </p>
    </div>
  );
}
