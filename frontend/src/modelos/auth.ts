import type { Rol } from './rol';

// Espejo de: record InicioSesionRequest(String correo, String contrasena)
export interface InicioSesionRequest {
  correo: string;
  contrasena: string;
}

// Espejo de: record TokenResponse(String token, String tipo, Instant expiraEn, String correo, Rol rol, List<Rol> roles)
export interface TokenResponse {
  token: string;
  tipo: string;       // "Bearer"
  expiraEn: string;   // Instant llega como texto ISO-8601: "2026-11-20T15:30:00Z"
  correo: string;
  rol: Rol;
  roles: Rol[];
}

// Espejo de: record RegistroHuespedRequest(...). No lleva rol: el registro público siempre crea un HUESPED.
export interface RegistroHuespedRequest {
  correo: string;
  contrasena: string;
  nombreCompleto: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string;   // LocalDate viaja como "AAAA-MM-DD"
}

// Espejo de: record CrearUsuarioRequest(...). Solo el administrador lo usa: él asigna el rol.
export interface CrearUsuarioRequest extends RegistroHuespedRequest {
  rol: Rol;
}

// Espejo de: record UsuarioResponse(...)
export interface UsuarioResponse {
  identificacion: string;
  correo: string;
  nombreCompleto: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string;
  rol: Rol;
  activo: boolean;
}

// Espejo de: record SolicitarRecuperacionRequest(String correo)
export interface SolicitarRecuperacionRequest {
  correo: string;
}

// Espejo de: record RestablecerContrasenaRequest(String correo, String codigo, String contrasenaNueva)
export interface RestablecerContrasenaRequest {
  correo: string;
  codigo: string;
  contrasenaNueva: string;
}
