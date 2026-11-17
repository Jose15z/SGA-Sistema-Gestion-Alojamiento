import type {
  Alojamiento,
  Apartamento,
  ApartamentoDisponible,
  FiltroDisponibles,
  NocheCalendario,
  PoliticaCancelacion,
} from '../modelos/apartamento';
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
export async function cotizarEstancia(datos: CotizacionRequest, signal?: AbortSignal): Promise<Cotizacion> {
  const respuesta = await api.post<Cotizacion>('/cotizaciones', datos, { signal });
  return respuesta.data;
}

// GET /api/alojamiento: información general, horarios, normas y servicios adicionales (público).
export async function consultarAlojamiento(signal?: AbortSignal): Promise<Alojamiento> {
  const respuesta = await api.get<Alojamiento>('/alojamiento', { signal });
  return respuesta.data;
}

// GET /api/politicas-cancelacion/vigente: tramos vigentes y porcentaje de no-show (público).
export async function consultarPoliticaVigente(signal?: AbortSignal): Promise<PoliticaCancelacion> {
  const respuesta = await api.get<PoliticaCancelacion>('/politicas-cancelacion/vigente', { signal });
  return respuesta.data;
}

// GET /api/apartamentos/disponibles: búsqueda paginada con valor estimado calculado en Spring Boot.
// paramsSerializer: { indexes: null } serializa listas como ?caracteristicas=A&caracteristicas=B para Spring MVC.
export async function buscarApartamentosDisponibles(
  filtro: FiltroDisponibles,
  signal?: AbortSignal,
): Promise<Pagina<ApartamentoDisponible>> {
  const respuesta = await api.get<Pagina<ApartamentoDisponible>>('/apartamentos/disponibles', {
    params: filtro,
    paramsSerializer: { indexes: null },
    signal,
  });
  return respuesta.data;
}

// GET /api/apartamentos/{identificacion}: detalle completo del apartamento (público).
export async function consultarApartamento(identificacion: string, signal?: AbortSignal): Promise<Apartamento> {
  const respuesta = await api.get<Apartamento>(`/apartamentos/${encodeURIComponent(identificacion)}`, { signal });
  return respuesta.data;
}

// GET /api/apartamentos/{identificacion}/calendario: disponibilidad nocturna en [desde, hasta) (público).
export async function consultarCalendarioApartamento(
  identificacion: string,
  desde: string,
  hasta: string,
  signal?: AbortSignal,
): Promise<NocheCalendario[]> {
  const respuesta = await api.get<NocheCalendario[]>(
    `/apartamentos/${encodeURIComponent(identificacion)}/calendario`,
    {
      params: { desde, hasta },
      signal,
    },
  );
  return respuesta.data;
}
