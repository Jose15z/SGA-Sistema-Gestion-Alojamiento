import { Icono } from './Icono';

interface PantallaPendienteProps {
  // Nombre del archivo en frontend/mockups/pantallas con el diseño de la pantalla.
  mockup: string;
}

export function PantallaPendiente({ mockup }: PantallaPendienteProps) {
  return (
    <div className="empty">
      <Icono nombre="reloj" tamano={28} />
      <h3>Pantalla en construcción</h3>
      <p>Diseño de referencia: mockups/pantallas/{mockup}</p>
    </div>
  );
}
