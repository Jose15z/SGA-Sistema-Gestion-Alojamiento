// Fecha de hoy como "AAAA-MM-DD" en la hora LOCAL.
// Nunca toISOString(): devuelve la fecha en UTC y en la noche de Colombia ya es "mañana".
export function hoyISO(): string {
  const hoy = new Date();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const dia = String(hoy.getDate()).padStart(2, '0');
  return `${hoy.getFullYear()}-${mes}-${dia}`;
}
