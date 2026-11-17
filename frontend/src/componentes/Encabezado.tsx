import { Link } from 'react-router';
import { Icono } from './Icono';

interface EncabezadoProps {
  titulo: string;
  // La recepción y la administración crean reservas por el canal directo.
  mostrarNuevaReserva: boolean;
  menuAbierto: boolean;
  onAbrirMenu: () => void;
}

export function Encabezado({ titulo, mostrarNuevaReserva, menuAbierto, onAbrirMenu }: EncabezadoProps) {
  return (
    <header className="topbar">
      <button
        className="menu-button"
        onClick={onAbrirMenu}
        aria-label="Abrir menú"
        aria-expanded={menuAbierto}
        aria-controls="barra-lateral"
      >
        <Icono nombre="menu" />
      </button>
      <div>
        <strong>{titulo}</strong>
        <span>Armenia, Quindío</span>
      </div>
      <div className="top-actions">
        {mostrarNuevaReserva && (
          <Link className="primary compact" to="/recepcion/nueva-reserva">
            <Icono nombre="mas" /> Nueva reserva
          </Link>
        )}
      </div>
    </header>
  );
}
