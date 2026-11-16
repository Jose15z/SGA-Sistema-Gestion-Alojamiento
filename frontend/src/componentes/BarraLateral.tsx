import { Link, NavLink } from 'react-router';
import { navegacionPorRol } from '../datos/navegacion';
import { nombreRol, type Rol } from '../modelos/rol';
import { Icono } from './Icono';
import { Marca } from './Marca';

// Usuario de ejemplo por rol mientras no hay sesión (la sesión llega en la Guía 15).
const usuarioEjemplo: Record<Rol, { nombre: string; iniciales: string }> = {
  HUESPED: { nombre: 'Ana Ramírez', iniciales: 'AR' },
  RECEPCIONISTA: { nombre: 'Mateo Ríos', iniciales: 'MR' },
  PERSONAL_SERVICIO: { nombre: 'Luisa Sánchez', iniciales: 'LS' },
  ADMINISTRADOR: { nombre: 'Admin SGA', iniciales: 'AD' },
};

interface BarraLateralProps {
  rol: Rol;
  abierta: boolean;
  onCambiarRol: (rol: Rol) => void;
  onCerrar: () => void;
}

export function BarraLateral({ rol, abierta, onCambiarRol, onCerrar }: BarraLateralProps) {
  const usuario = usuarioEjemplo[rol];
  const roles = Object.keys(nombreRol) as Rol[];

  return (
    <aside id="barra-lateral" className={`sidebar ${abierta ? 'open' : ''}`}>
      <button className="mobile-close" onClick={onCerrar} aria-label="Cerrar menú">
        <Icono nombre="cerrar" />
      </button>

      <Link className="brand" to={navegacionPorRol[rol][0].ruta} onClick={onCerrar}>
        <Marca />
      </Link>

      <label className="demo-label" htmlFor="selector-rol">
        ESPACIO DE TRABAJO
      </label>
      <div className="role-select-wrap">
        <Icono nombre="escudo" tamano={18} />
        <select
          id="selector-rol"
          value={rol}
          onChange={(e) => onCambiarRol(e.target.value as Rol)}
          title="Cambiar el espacio de trabajo según el rol"
        >
          {roles.map((r) => (
            <option key={r} value={r}>
              {nombreRol[r]}
            </option>
          ))}
        </select>
        <Icono nombre="desplegar" tamano={15} />
      </div>

      <nav className="main-nav" aria-label="Principal">
        {navegacionPorRol[rol].map((opcion) => (
          <NavLink key={opcion.ruta} to={opcion.ruta} end={opcion.ruta === '/recepcion'} onClick={onCerrar}>
            <Icono nombre={opcion.icono} />
            <span>{opcion.etiqueta}</span>
          </NavLink>
        ))}
      </nav>

      <div className="profile">
        <span className="avatar">{usuario.iniciales}</span>
        <span>
          <strong>{usuario.nombre}</strong>
          <small>{nombreRol[rol]}</small>
        </span>
        <Link to="/login" aria-label="Cerrar sesión" title="Cerrar sesión" className="profile-salir">
          <Icono nombre="salir" tamano={18} />
        </Link>
      </div>
    </aside>
  );
}
