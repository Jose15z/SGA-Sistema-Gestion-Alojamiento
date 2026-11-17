// Espejo del ErrorResponse del backend: la forma de TODOS los errores de la API.
export interface ErrorResponse {
  estado: number;
  codigo: string;          // "CREDENCIALES_INVALIDAS", "CONFLICTO_USUARIO", ...
  mensaje: string;         // mensaje con significado de negocio
  detalles: string[];      // en un 400 de Bean Validation: "campo: mensaje"
  marcaTiempo: string;
}
