import axios from 'axios';
import type { ErrorResponse } from '../modelos/error';

/**
 * Traduce cualquier error de una petición a un mensaje para el usuario.
 * Si el backend envió un ErrorResponse, se usa su mensaje de negocio.
 */
export function mensajeDeError(error: unknown): string {
  if (!axios.isAxiosError<ErrorResponse>(error)) {
    return 'Ocurrió un error inesperado.';
  }
  if (!error.response) {
    // Sin respuesta: el backend está apagado, la URL está mal o CORS bloqueó la petición
    return 'No fue posible conectar con el servidor.';
  }
  const { status, data } = error.response;
  if (data?.mensaje) {
    return data.mensaje;
  }
  if (status === 403) {
    return 'No tiene permisos para realizar esta acción.';
  }
  return `Error inesperado del servidor (${status}).`;
}

/** Código HTTP de la respuesta, o undefined si no hubo respuesta. */
export function estadoHttp(error: unknown): number | undefined {
  return axios.isAxiosError(error) ? error.response?.status : undefined;
}

/**
 * Errores de campo de un 400 de Bean Validation. El backend los envía en `detalles`
 * como "campo: mensaje"; se devuelven con el nombre de campo de React Hook Form
 * ("ocupantes[1].documento" → "ocupantes.1.documento").
 */
export function erroresDeCampo(error: unknown): Record<string, string> {
  if (!axios.isAxiosError<ErrorResponse>(error) || error.response?.status !== 400) {
    return {};
  }
  const resultado: Record<string, string> = {};
  for (const detalle of error.response.data?.detalles ?? []) {
    const separador = detalle.indexOf(': ');
    if (separador > 0) {
      const campo = detalle.slice(0, separador).replace(/\[(\d+)\]/g, '.$1');
      resultado[campo] = detalle.slice(separador + 2);
    }
  }
  return resultado;
}
