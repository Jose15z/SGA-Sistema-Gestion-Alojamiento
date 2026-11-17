import { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router';
import { BarraLateral } from '../componentes/BarraLateral';
import { Encabezado } from '../componentes/Encabezado';
import { PiePagina } from '../componentes/PiePagina';
import { inicioDeRol, rutaPermitida, tituloDeSeccion } from '../datos/navegacion';
import { borrarSesion, leerSesion } from '../servicios/sesion';

// Estructura de las pantallas internas: barra lateral por rol, barra superior y contenido.
export default function DisenoAplicacion() {
  const { pathname } = useLocation();
  const navegar = useNavigate();
  const [menuAbierto, setMenuAbierto] = useState(false);

  // Con el menú abierto en el celular, la tecla Esc lo cierra.
  useEffect(() => {
    if (!menuAbierto) return;
    function alPresionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') setMenuAbierto(false);
    }
    window.addEventListener('keydown', alPresionarTecla);
    return () => window.removeEventListener('keydown', alPresionarTecla);
  }, [menuAbierto]);

  // Sin sesión (o con el token vencido) se vuelve al login, recordando a dónde se quería ir.
  const sesion = leerSesion();
  if (!sesion) {
    return <Navigate to="/login" replace state={{ desde: pathname }} />;
  }
  // Cada rol solo ve sus pantallas; el backend igual rechaza con 403 lo que no le corresponde.
  if (!rutaPermitida(sesion.rol, pathname)) {
    return <Navigate to={inicioDeRol(sesion.rol)} replace />;
  }

  function cerrarSesion() {
    borrarSesion();
    navegar('/login', { replace: true });
  }

  return (
    <div className="app-shell">
      <a className="saltar-contenido" href="#contenido">
        Saltar al contenido
      </a>
      <BarraLateral
        rol={sesion.rol}
        correo={sesion.correo}
        abierta={menuAbierto}
        onCerrar={() => setMenuAbierto(false)}
        onCerrarSesion={cerrarSesion}
      />
      {menuAbierto && <div className="scrim" onClick={() => setMenuAbierto(false)} />}

      <main className="main">
        <Encabezado
          titulo={tituloDeSeccion(pathname)}
          mostrarNuevaReserva={sesion.rol === 'RECEPCIONISTA' || sesion.rol === 'ADMINISTRADOR'}
          menuAbierto={menuAbierto}
          onAbrirMenu={() => setMenuAbierto(true)}
        />
        <div className="content" id="contenido" tabIndex={-1}>
          <Outlet />
          <PiePagina />
        </div>
      </main>
    </div>
  );
}
