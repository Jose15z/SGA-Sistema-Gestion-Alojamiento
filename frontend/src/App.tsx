import { Navigate, Route, Routes } from 'react-router';
import DisenoAcceso from './disenos/DisenoAcceso';
import DisenoAplicacion from './disenos/DisenoAplicacion';
import Administracion from './paginas/Administracion';
import DetalleApartamento from './paginas/DetalleApartamento';
import DetalleReserva from './paginas/DetalleReserva';
import Folio from './paginas/Folio';
import Inicio from './paginas/Inicio';
import Login from './paginas/Login';
import MisReservas from './paginas/MisReservas';
import NoEncontrada from './paginas/NoEncontrada';
import NuevaReserva from './paginas/NuevaReserva';
import NuevaReservaDirecta from './paginas/NuevaReservaDirecta';
import Preparacion from './paginas/Preparacion';
import Recepcion from './paginas/Recepcion';
import Registro from './paginas/Registro';
import Reportes from './paginas/Reportes';
import ReservasYFolios from './paginas/ReservasYFolios';

export default function App() {
  return (
    <Routes>
      {/* Acceso: login y registro */}
      <Route element={<DisenoAcceso />}>
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Registro />} />
      </Route>

      {/* Pantallas internas, con la barra lateral por rol */}
      <Route element={<DisenoAplicacion />}>
        <Route index element={<Navigate to="/apartamentos" replace />} />

        {/* Huésped */}
        <Route path="/apartamentos" element={<Inicio />} />
        <Route path="/apartamentos/:identificacion" element={<DetalleApartamento />} />
        <Route path="/reservas/nueva" element={<NuevaReserva />} />
        <Route path="/mis-reservas" element={<MisReservas />} />
        <Route path="/mis-reservas/:codigo" element={<DetalleReserva />} />

        {/* Recepción */}
        <Route path="/recepcion" element={<Recepcion />} />
        <Route path="/recepcion/nueva-reserva" element={<NuevaReservaDirecta />} />
        <Route path="/recepcion/reservas" element={<ReservasYFolios />} />
        <Route path="/recepcion/reservas/:codigo/folio" element={<Folio />} />

        {/* Personal de servicio y administración */}
        <Route path="/preparacion" element={<Preparacion />} />
        <Route path="/administracion" element={<Administracion />} />
        <Route path="/reportes" element={<Reportes />} />

        <Route path="*" element={<NoEncontrada />} />
      </Route>
    </Routes>
  );
}
