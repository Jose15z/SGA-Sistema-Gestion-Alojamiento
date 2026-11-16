// Se calcula una sola vez, al cargar el módulo: un componente debe ser puro
// y no consultar el reloj en cada render.
const ANIO_ACTUAL = new Date().getFullYear();

export function PiePagina() {
  return <footer>© {ANIO_ACTUAL} Universidad del Quindío — Programación Avanzada</footer>;
}
