import type { TokenResponse } from '../modelos/auth';

// Una sola clave para toda la sesión: token, tipo, expiración, correo y rol.
const CLAVE_SESION = 'sga.sesion';

// "Recordarme": en localStorage la sesión sobrevive al cerrar el navegador;
// en sessionStorage se borra al cerrarlo.
export function guardarSesion(sesion: TokenResponse, recordar: boolean): void {
  borrarSesion();
  const almacen = recordar ? localStorage : sessionStorage;
  almacen.setItem(CLAVE_SESION, JSON.stringify(sesion));
}

// Devuelve la sesión solo si el token no ha vencido (dura 30 minutos, Guía 12).
export function leerSesion(): TokenResponse | null {
  const texto = localStorage.getItem(CLAVE_SESION) ?? sessionStorage.getItem(CLAVE_SESION);
  if (!texto) {
    return null;
  }
  const sesion = JSON.parse(texto) as TokenResponse;
  if (new Date(sesion.expiraEn).getTime() <= Date.now()) {
    borrarSesion();
    return null;
  }
  return sesion;
}

export function obtenerToken(): string | null {
  return leerSesion()?.token ?? null;
}

// Cabecera para las peticiones que exigen sesión.
export function encabezadoAutorizacion(): { Authorization: string } | undefined {
  const token = obtenerToken();
  return token ? { Authorization: `Bearer ${token}` } : undefined;
}

export function borrarSesion(): void {
  localStorage.removeItem(CLAVE_SESION);
  sessionStorage.removeItem(CLAVE_SESION);
}
