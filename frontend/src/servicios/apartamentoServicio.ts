import type { Apartamento } from '../modelos/apartamento';
import type { Cotizacion, CotizacionRequest, Pagina } from '../modelos/reserva';
import { api } from './api';
import { encabezadoAutorizacion } from './sesion';

// GET /api/apartamentos: apartamentos activos con su estado operativo (recepción y administración).
// El alojamiento tiene 7 (Ficha), así que una página de 10 los trae todos.
export async function listarApartamentos(): Promise<Apartamento[]> {
  const respuesta = await api.get<Pagina<Apartamento>>('/apartamentos', {
    headers: encabezadoAutorizacion(),
    params: { size: 10 },
  });
  return respuesta.data.contenido;
}

// POST /api/cotizaciones: valor de una estancia según las temporadas y los ocupantes facturables
export async function cotizarEstancia(datos: CotizacionRequest): Promise<Cotizacion> {
  const respuesta = await api.post<Cotizacion>('/cotizaciones', datos);
  return respuesta.data;
}
