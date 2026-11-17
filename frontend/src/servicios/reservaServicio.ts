import type {
  CrearReservaRequest,
  FiltroReservas,
  MovimientosDelDia,
  Pagina,
  ReservaDetalle,
  ReservaResumen,
} from '../modelos/reserva';
import { api } from './api';
import { encabezadoAutorizacion } from './sesion';

const conSesion = () => ({ headers: encabezadoAutorizacion() });

// POST /api/reservas
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaDetalle> {
  const respuesta = await api.post<ReservaDetalle>('/reservas', datos, conSesion());
  return respuesta.data;
}

// GET /api/reservas: listado paginado con filtros (recepción y administración)
export async function listarReservas(filtro: FiltroReservas): Promise<Pagina<ReservaResumen>> {
  const respuesta = await api.get<Pagina<ReservaResumen>>('/reservas', { ...conSesion(), params: filtro });
  return respuesta.data;
}

// GET /api/reservas/movimientos-del-dia: llegadas y salidas previstas de un día
export async function consultarMovimientosDelDia(fecha: string): Promise<MovimientosDelDia> {
  const respuesta = await api.get<MovimientosDelDia>('/reservas/movimientos-del-dia', {
    ...conSesion(),
    params: { fecha },
  });
  return respuesta.data;
}

// GET /api/reservas/{codigo}
export async function consultarReserva(codigo: string): Promise<ReservaDetalle> {
  const respuesta = await api.get<ReservaDetalle>(`/reservas/${codigo}`, conSesion());
  return respuesta.data;
}

// Transiciones con intención de negocio: no hay un "cambiar estado" genérico.
type Accion = 'confirmar' | 'registrar-llegada' | 'registrar-salida' | 'no-show';

export async function cambiarEstadoReserva(codigo: string, accion: Accion): Promise<ReservaDetalle> {
  const respuesta = await api.put<ReservaDetalle>(`/reservas/${codigo}/${accion}`, null, conSesion());
  return respuesta.data;
}

// PUT /api/reservas/{codigo}/cancelar
export async function cancelarReserva(codigo: string, motivo: string): Promise<ReservaDetalle> {
  const respuesta = await api.put<ReservaDetalle>(`/reservas/${codigo}/cancelar`, { motivo }, conSesion());
  return respuesta.data;
}
