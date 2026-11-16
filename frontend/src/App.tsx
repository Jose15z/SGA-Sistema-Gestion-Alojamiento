import { Route, Routes } from 'react-router';
import { Encabezado } from './componentes/Encabezado';
import { PiePagina } from './componentes/PiePagina';
import Administracion from './paginas/Administracion';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import Login from './paginas/Login';
import MisReservas from './paginas/MisReservas';
import NoEncontrada from './paginas/NoEncontrada';
import Preparacion from './paginas/Preparacion';
import Recepcion from './paginas/Recepcion';
import Registro from './paginas/Registro';

export default function App() {
  return (
    <>
      <Encabezado nombreAlojamiento="Siete Raíces – Apartamentos Turísticos" />

      <main>
        <Routes>
          {/* Inicio es la búsqueda de disponibilidad del huésped (7.5) */}
          <Route path="/" element={<Inicio />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/apartamentos/:identificacion" element={<DetalleApartamento />} />
          <Route path="/mis-reservas" element={<MisReservas />} />
          <Route path="/recepcion" element={<Recepcion />} />
          <Route path="/preparacion" element={<Preparacion />} />
          <Route path="/administracion" element={<Administracion />} />
          <Route path="*" element={<NoEncontrada />} />
        </Routes>
      </main>

      <PiePagina />
    </>
  );
}
