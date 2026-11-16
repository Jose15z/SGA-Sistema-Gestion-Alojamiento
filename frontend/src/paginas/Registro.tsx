import { PantallaPendiente } from '../componentes/PantallaPendiente';

export default function Registro() {
  return (
    <>
      <div className="auth-heading">
        <span className="eyebrow">NUEVA CUENTA</span>
        <h2>Regístrate como huésped</h2>
        <p>El registro público crea usuarios con rol Huésped.</p>
      </div>
      {/* El formulario con React Hook Form y Zod se construye en la Guía 15. */}
      <PantallaPendiente mockup="02-registro.png" />
    </>
  );
}
