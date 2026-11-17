import { Navigate, NavLink, Outlet, useLocation } from 'react-router';
import { Etiqueta } from '../componentes/Etiqueta';
import { Icono } from '../componentes/Icono';
import { Marca } from '../componentes/Marca';
import { inicioDeRol } from '../datos/navegacion';
import { leerSesion } from '../servicios/sesion';

// Estructura del login y el registro: foto del alojamiento a la izquierda y el formulario a la derecha.
export default function DisenoAcceso() {
  const { pathname } = useLocation();
  const esRegistro = pathname === '/registro';

  // Quien ya tiene sesión no necesita ingresar otra vez: va a la pantalla de su rol.
  const sesion = leerSesion();
  if (sesion) {
    return <Navigate to={inicioDeRol(sesion.rol)} replace />;
  }

  return (
    <main className="auth-shell">
      <section className="auth-visual" aria-label="Siete Raíces Apartamentos turísticos">
        <div className="auth-brand">
          <Marca tamanoIcono={27} />
        </div>
        <div className="auth-story">
          <Etiqueta tono="light">
            <Icono nombre="mapa" tamano={14} /> Armenia, Quindío
          </Etiqueta>
          <h1>
            Hospitalidad que nace
            <br />
            <em>desde nuestras raíces.</em>
          </h1>
          <p>Una experiencia tranquila para huéspedes y una operación clara para todo el equipo.</p>
        </div>
        <div className="auth-proof">
          <span>
            <Icono nombre="escudo" /> Acceso seguro por roles
          </span>
          <span>
            <Icono nombre="reloj" /> Operación disponible 24/7
          </span>
        </div>
      </section>

      <section className="auth-form-side">
        <div className={`auth-card ${esRegistro ? 'register' : ''}`}>
          <div className="auth-mobile-brand">
            <span className="brand-mark">
              <Icono nombre="hoja" tamano={24} />
            </span>
            <strong>Siete Raíces</strong>
          </div>
          <nav className="auth-tabs" aria-label="Acceso a la cuenta">
            <NavLink to="/login">Ingresar</NavLink>
            <NavLink to="/registro">Registrarse</NavLink>
          </nav>
          <Outlet />
        </div>
      </section>
    </main>
  );
}
