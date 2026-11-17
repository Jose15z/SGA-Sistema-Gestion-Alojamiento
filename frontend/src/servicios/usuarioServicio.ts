import type { CrearUsuarioRequest, UsuarioResponse } from '../modelos/auth';
import { api } from './api';
import { encabezadoAutorizacion } from './sesion';

// POST /api/usuarios: solo el ADMINISTRADOR; el backend responde 403 a cualquier otro rol
export async function crearUsuario(datos: CrearUsuarioRequest): Promise<UsuarioResponse> {
  const respuesta = await api.post<UsuarioResponse>('/usuarios', datos, {
    headers: encabezadoAutorizacion(),
  });
  return respuesta.data;
}
