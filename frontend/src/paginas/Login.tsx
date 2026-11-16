import { Link } from 'react-router';
import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Login() {
  return (
    <>
      <div className="auth-heading">
        <span className="eyebrow">BIENVENIDO DE NUEVO</span>
        <h2>Ingresa a Siete Raíces</h2>
        <p>Reserva tu estancia o continúa con la operación del alojamiento.</p>
      </div>
      {/* El formulario con React Hook Form y Zod se construye en la Guía 15. */}
      <PantallaPendiente mockup="01-login.png" />
      <p className="auth-switch">
        <Link to="/apartamentos">Entrar sin sesión (provisional)</Link>
      </p>
    </>
  );
}
