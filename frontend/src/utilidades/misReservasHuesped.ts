const PREFIJO_CLAVE = 'sga.mis_reservas';
const PATRON_CODIGO = /^RES-[0-9]{4}-[0-9]{5}$/;

function claveUsuario(identificadorUsuario: string): string {
  const limpio = identificadorUsuario.trim();
  return `${PREFIJO_CLAVE}.${limpio}`;
}

export function esCodigoReservaValido(codigo: string): boolean {
  return PATRON_CODIGO.test(codigo.trim().toUpperCase());
}

/**
 * Lee los códigos de reserva recordados localmente para el huésped autenticado.
 * No sustituye a una consulta al servidor ni almacena tokens o datos personales.
 */
export function leerCodigosReserva(identificadorUsuario: string): string[] {
  const clave = claveUsuario(identificadorUsuario);
  if (!identificadorUsuario.trim()) return [];
  try {
    const bruto = window.localStorage.getItem(clave);
    if (!bruto) return [];
    const parsed: unknown = JSON.parse(bruto);
    if (!Array.isArray(parsed)) return [];
    const unicos = new Set<string>();
    for (const item of parsed) {
      if (typeof item === 'string') {
        const normalizado = item.trim().toUpperCase();
        if (esCodigoReservaValido(normalizado)) {
          unicos.add(normalizado);
        }
      }
    }
    return Array.from(unicos);
  } catch {
    return [];
  }
}

/** Guarda un código válido `RES-AAAA-NNNNN` sin duplicados para el usuario autenticado. */
export function recordarCodigoReserva(identificadorUsuario: string, codigo: string): void {
  const normalizado = codigo.trim().toUpperCase();
  if (!identificadorUsuario.trim() || !esCodigoReservaValido(normalizado)) return;
  try {
    const actuales = leerCodigosReserva(identificadorUsuario);
    if (actuales.includes(normalizado)) return;
    const actualizados = [normalizado, ...actuales];
    window.localStorage.setItem(claveUsuario(identificadorUsuario), JSON.stringify(actualizados));
  } catch {
    // Se ignora si el almacenamiento local está bloqueado o sin cuota disponible.
  }
}

/** Elimina un código puntual de la lista recordada del usuario autenticado. */
export function olvidarCodigoReserva(identificadorUsuario: string, codigo: string): void {
  const normalizado = codigo.trim().toUpperCase();
  if (!identificadorUsuario.trim() || !esCodigoReservaValido(normalizado)) return;
  try {
    const filtrados = leerCodigosReserva(identificadorUsuario).filter((c) => c !== normalizado);
    window.localStorage.setItem(claveUsuario(identificadorUsuario), JSON.stringify(filtrados));
  } catch {
    // Se ignora si el almacenamiento local no está disponible.
  }
}

/** Limpia todos los códigos recordados del usuario autenticado. */
export function limpiarCodigosReserva(identificadorUsuario: string): void {
  if (!identificadorUsuario.trim()) return;
  try {
    window.localStorage.removeItem(claveUsuario(identificadorUsuario));
  } catch {
    // Se ignora si el almacenamiento local no está disponible.
  }
}
