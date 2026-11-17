import type { Folio, Pago, RegistrarPagoRequest } from '../modelos/folio';
import { api } from './api';
import { encabezadoAutorizacion } from './sesion';

const conSesion = () => ({ headers: encabezadoAutorizacion() });

// GET /api/folios/{codigoReserva}
export async function consultarFolio(codigoReserva: string): Promise<Folio> {
  const respuesta = await api.get<Folio>(`/folios/${codigoReserva}`, conSesion());
  return respuesta.data;
}

// POST /api/folios/{codigoReserva}/pagos
export async function registrarPago(codigoReserva: string, pago: RegistrarPagoRequest): Promise<Pago> {
  const respuesta = await api.post<Pago>(`/folios/${codigoReserva}/pagos`, pago, conSesion());
  return respuesta.data;
}

// PUT /api/folios/{codigoReserva}/cerrar. Con saldo distinto de cero solo lo cierra
// el administrador, indicando el motivo (RN-17).
export async function cerrarFolio(codigoReserva: string, motivo: string | null): Promise<Folio> {
  const respuesta = await api.put<Folio>(`/folios/${codigoReserva}/cerrar`, { motivo }, conSesion());
  return respuesta.data;
}
