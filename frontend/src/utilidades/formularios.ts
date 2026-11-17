import type { FieldValues, Path, UseFormSetError } from 'react-hook-form';
import { erroresDeCampo, estadoHttp, mensajeDeError } from './errores';

/**
 * Muestra el error del servidor donde corresponde:
 * - 400 de Bean Validation: junto a cada campo.
 * - 409 de correo o documento ya registrados: junto a ese campo.
 * - Cualquier otro: arriba del botón (root.servidor).
 */
export function mostrarErrorDelServidor<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  campos: readonly Path<T>[],
): void {
  const porCampo = erroresDeCampo(error);
  let ubicados = 0;
  for (const campo of campos) {
    if (porCampo[campo]) {
      setError(campo, { message: porCampo[campo] });
      ubicados++;
    }
  }
  if (ubicados > 0) return;

  const mensaje = mensajeDeError(error);
  if (estadoHttp(error) === 409) {
    const campo = campos.find((c) => mensaje.toLowerCase().includes(String(c).toLowerCase()));
    if (campo) {
      setError(campo, { message: mensaje });
      return;
    }
  }
  setError('root.servidor' as Path<T>, { message: mensaje });
}

// Atributos de accesibilidad de un campo según tenga o no error.
export function atributosDeError(id: string, error?: string) {
  return {
    'aria-invalid': error ? true : undefined,
    'aria-describedby': error ? `${id}-error` : undefined,
  };
}
