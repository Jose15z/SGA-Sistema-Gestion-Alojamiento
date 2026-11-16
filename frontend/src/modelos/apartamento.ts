// Vista breve de un apartamento, tal como la mostrará el frontend.
// En la Guía 15 este tipo se alinea con el DTO que expone el backend.
export interface ApartamentoResumen {
  identificacion: string;   // "APT-301": la identidad del negocio (L-04)
  nombre: string;
  dormitorios: number;
  capacidad: number;        // tope rígido de personas (F-03)
}
