import { Link, NavLink } from 'react-router';
import { inicioDeRol, navegacionPorRol } from '../datos/navegacion';
import { nombreRol, type Rol } from '../modelos/rol';
import { Icono } from './Icono';
import { Marca } from './Marca';

interface BarraLateralProps {
  rol: Rol;
  correo: string;
  abierta: boolean;
  onCerrar: () => void;
  onCerrarSesion: () => void;
}

// Las opciones con subpantallas propias se marcan solo en su ruta exacta.
const rutasExactas = new Set(['/recepcion', '/administracion']);

export function BarraLateral({ rol, correo, abierta, onCerrar, onCerrarSesion }: BarraLateralProps) {
  const iniciales = correo.slice(0, 2).toUpperCase();

  return (
    <aside id="barra-lateral" className={`sidebar ${abierta ? 'open' : ''}`}>
      <button className="mobile-close" onClick={onCerrar} aria-label="Cerrar menú">
        <Icono nombre="cerrar" />
      </button>

      <Link className="brand" to={inicioDeRol(rol)} onClick={onCerrar}>
        <Marca />
      </Link>

      {/* El rol lo define la cuenta con la que se ingresó: se muestra, no se escoge. */}
      <span className="demo-label">ESPACIO DE TRABAJO</span>
      <div className="role-select-wrap rol-sesion" title="Rol de la cuenta con la que ingresó">
        <Icono nombre="escudo" tamano={18} />
        <span>{nombreRol[rol]}</span>
      </div>

      <nav className="main-nav" aria-label="Principal">
        {navegacionPorRol[rol].map((opcion) => (
          <NavLink key={opcion.ruta} to={opcion.ruta} end={rutasExactas.has(opcion.ruta)} onClick={onCerrar}>
            <Icono nombre={opcion.icono} />
            <span>{opcion.etiqueta}</span>
          </NavLink>
        ))}
      </nav>

      <div className="profile">
        <span className="avatar">{iniciales}</span>
        <span>
          <strong>{correo}</strong>
          <small>{nombreRol[rol]}</small>
        </span>
        <button type="button" onClick={onCerrarSesion} aria-label="Cerrar sesión" title="Cerrar sesión" className="profile-salir">
          <Icono nombre="salir" tamano={18} />
        </button>
      </div>
    </aside>
  );
}
