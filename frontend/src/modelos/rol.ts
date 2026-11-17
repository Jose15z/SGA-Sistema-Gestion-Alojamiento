// Los mismos roles que el backend (application/seguridad/Rol).
export type Rol = 'HUESPED' | 'RECEPCIONISTA' | 'PERSONAL_SERVICIO' | 'ADMINISTRADOR';

export const nombreRol: Record<Rol, string> = {
  HUESPED: 'Huésped',
  RECEPCIONISTA: 'Recepción',
  PERSONAL_SERVICIO: 'Personal de servicio',
  ADMINISTRADOR: 'Administrador',
};
