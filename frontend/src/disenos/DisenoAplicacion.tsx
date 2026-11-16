import { useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router';
import { BarraLateral } from '../componentes/BarraLateral';
import { Encabezado } from '../componentes/Encabezado';
import { PiePagina } from '../componentes/PiePagina';
import { navegacionPorRol, rolDeRuta, tituloDeSeccion } from '../datos/navegacion';
import type { Rol } from '../modelos/rol';

// Estructura de las pantallas internas: barra lateral por rol, barra superior y contenido.
export default function DisenoAplicacion() {
  const { pathname } = useLocation();
  const navegar = useNavigate();

  // Mientras no hay sesión (Guía 15), el rol se escoge en la barra lateral.
  const [rol, setRol] = useState<Rol>(() => rolDeRuta(pathname));
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

  function cambiarRol(nuevoRol: Rol) {
    setRol(nuevoRol);
    navegar(navegacionPorRol[nuevoRol][0].ruta);
  }

  return (
    <div className="app-shell">
      <a className="saltar-contenido" href="#contenido">
        Saltar al contenido
      </a>
      <BarraLateral
        rol={rol}
        abierta={menuAbierto}
        onCambiarRol={cambiarRol}
        onCerrar={() => setMenuAbierto(false)}
      />
      {menuAbierto && <div className="scrim" onClick={() => setMenuAbierto(false)} />}

      <main className="main">
        <Encabezado
          titulo={tituloDeSeccion(pathname)}
          mostrarNuevaReserva={rol === 'RECEPCIONISTA' || rol === 'ADMINISTRADOR'}
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
