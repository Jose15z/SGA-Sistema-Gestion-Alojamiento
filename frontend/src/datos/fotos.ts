import { apartamentosEjemplo } from './apartamentosEjemplo';

// Fotos de los apartamentos (public/fotos). Mientras el backend no guarde imágenes,
// cada apartamento de la Ficha tiene asignada una de las fotos del diseño.
const fotoPorApartamento: Record<string, string> = {
  'APT-101': '/fotos/palma.jpg',
  'APT-102': '/fotos/cocora.jpg',
  'APT-201': '/fotos/palma.jpg',
  'APT-202': '/fotos/barranquero.jpg',
  'APT-301': '/fotos/orquidea.jpg',
  'APT-302': '/fotos/barranquero.jpg',
  'APT-303': '/fotos/cocora.jpg',
};

export function fotoDeApartamento(identificacion: string): string {
  return fotoPorApartamento[identificacion] ?? '/fotos/orquidea.jpg';
}

// "APT-301" → "APT-301 · Orquídea", con los nombres de la Ficha.
export function nombreDeApartamento(identificacion: string): string {
  const apartamento = apartamentosEjemplo.find((a) => a.identificacion === identificacion);
  return apartamento ? `${identificacion} · ${apartamento.nombre}` : identificacion;
}
