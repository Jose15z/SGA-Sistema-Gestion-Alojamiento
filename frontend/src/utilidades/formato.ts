// Formatos para mostrar valores al usuario, siempre en español de Colombia.

const PESOS = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

export function pesos(valor: number): string {
  return PESOS.format(valor);
}

// "2026-11-20" → "20 nov". Se arma la fecha con sus partes para no correrla por la zona horaria.
export function fechaCorta(iso: string): string {
  const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number);
  const partes = new Intl.DateTimeFormat('es-CO', { day: '2-digit', month: 'short' }).formatToParts(
    new Date(anio, mes - 1, dia),
  );
  const valor = (tipo: string) => partes.find((p) => p.type === tipo)?.value.replace('.', '') ?? '';
  return `${valor('day')} ${valor('month')}`;
}

// "2026-11-20" → "viernes, 20 de noviembre de 2026"
export function fechaLarga(iso: string): string {
  const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number);
  return new Date(anio, mes - 1, dia).toLocaleDateString('es-CO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}

// "2026-11-20T15:30:00" → "20 nov, 3:30 p. m."
export function fechaHora(iso: string): string {
  return new Date(iso).toLocaleString('es-CO', {
    day: '2-digit',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
  });
}

// "16:30:00" → "16:30"
export function hora(texto: string | null): string {
  return texto ? texto.slice(0, 5) : '—';
}

// Noches entre dos fechas "AAAA-MM-DD".
export function noches(entrada: string, salida: string): number {
  const dias = (Date.parse(salida) - Date.parse(entrada)) / 86_400_000;
  return Math.round(dias);
}

// Suma días a una fecha "AAAA-MM-DD" sin pasar por UTC.
export function sumarDias(iso: string, dias: number): string {
  const [anio, mes, dia] = iso.split('-').map(Number);
  const fecha = new Date(anio, mes - 1, dia + dias);
  const m = String(fecha.getMonth() + 1).padStart(2, '0');
  const d = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${m}-${d}`;
}
