// Indicador mientras llega la respuesta del backend (filas de esqueleto con brillo).
export function Cargando({ filas = 3, texto = 'Cargando…' }: { filas?: number; texto?: string }) {
  return (
    <div className="cargando" role="status" aria-live="polite">
      <span className="sr-only">{texto}</span>
      {Array.from({ length: filas }, (_, i) => (
        <div key={i} className="esqueleto" />
      ))}
    </div>
  );
}
