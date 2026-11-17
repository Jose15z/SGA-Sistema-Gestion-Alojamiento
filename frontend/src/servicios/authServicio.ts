import type {
  InicioSesionRequest,
  RegistroHuespedRequest,
  RestablecerContrasenaRequest,
  SolicitarRecuperacionRequest,
  TokenResponse,
  UsuarioResponse,
} from '../modelos/auth';
import { api } from './api';

// POST /api/auth/login (Guía 12)
export async function iniciarSesion(credenciales: InicioSesionRequest): Promise<TokenResponse> {
  const respuesta = await api.post<TokenResponse>('/auth/login', credenciales);
  return respuesta.data;
}

// POST /api/auth/registro: el backend siempre crea el usuario con rol HUESPED (7.2)
export async function registrarHuesped(datos: RegistroHuespedRequest): Promise<UsuarioResponse> {
  const respuesta = await api.post<UsuarioResponse>('/auth/registro', datos);
  return respuesta.data;
}

// POST /api/auth/recuperacion: responde 202 exista o no el correo
export async function solicitarRecuperacion(datos: SolicitarRecuperacionRequest): Promise<void> {
  await api.post('/auth/recuperacion', datos);
}

// POST /api/auth/recuperacion/restablecer
export async function restablecerContrasena(datos: RestablecerContrasenaRequest): Promise<void> {
  await api.post('/auth/recuperacion/restablecer', datos);
}
