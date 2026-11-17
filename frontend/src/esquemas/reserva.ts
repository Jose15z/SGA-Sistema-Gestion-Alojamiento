import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

// Un ocupante: los mismos límites de OcupanteRequest (@Size 30 y 150, @Past).
const esquemaOcupante = z.object({
  documento: z.string().trim().min(1, 'Ingrese el documento').max(30, 'Máximo 30 caracteres'),
  nombreCompleto: z.string().trim().min(1, 'Ingrese el nombre completo').max(150, 'Máximo 150 caracteres'),
  fechaNacimiento: z
    .string()
    .min(1, 'Ingrese la fecha de nacimiento')
    .refine((fecha) => fecha < hoyISO(), 'Debe ser anterior a hoy'),
});

// Reserva por el canal DIRECTO. El primer ocupante es el titular.
export const esquemaReservaDirecta = z
  .object({
    identificacionApartamento: z.string().min(1, 'Seleccione un apartamento'),
    fechaEntrada: z.string().min(1, 'Ingrese la fecha de entrada'),
    fechaSalida: z.string().min(1, 'Ingrese la fecha de salida'),
    horaEstimadaLlegada: z.string(),   // opcional al crear; se exige antes de confirmar (RN-09)
    ocupantes: z.array(esquemaOcupante).min(1, 'La reserva necesita al menos el titular'),
  })
  // RN-04: no se crean reservas con entrada en el pasado
  .refine((d) => !d.fechaEntrada || d.fechaEntrada >= hoyISO(), {
    path: ['fechaEntrada'],
    message: 'La fecha de entrada no puede estar en el pasado',
  })
  // Estancia: la salida es posterior a la entrada
  .refine((d) => !d.fechaEntrada || !d.fechaSalida || d.fechaSalida > d.fechaEntrada, {
    path: ['fechaSalida'],
    message: 'La fecha de salida debe ser posterior a la de entrada',
  });

export type FormularioReservaDirecta = z.infer<typeof esquemaReservaDirecta>;

// Reserva del huésped por el canal PORTAL: el titular proviene de GET /api/usuarios/yo.
export function crearEsquemaReservaHuesped(documentoTitular: string, capacidadMaxima: number) {
  const docTitularNormalizado = documentoTitular.trim().toLowerCase();

  return z
    .object({
      fechaEntrada: z.string().min(1, 'Seleccione la fecha de entrada'),
      fechaSalida: z.string().min(1, 'Seleccione la fecha de salida'),
      horaEstimadaLlegada: z
        .string()
        .refine(
          (hora) => hora === '' || /^([01]\d|2[0-3]):[0-5]\d$/.test(hora),
          'Ingrese una hora válida en formato HH:mm',
        ),
      mascotas: z
        .number({ error: 'Seleccione la cantidad de mascotas' })
        .int('Debe ser un número entero')
        .min(0, 'No puede ser negativo')
        .max(1, 'Siete Raíces admite máximo 1 mascota por reserva'),
      acompanantes: z.array(esquemaOcupante),
    })
    .superRefine((datos, ctx) => {
      const hoy = hoyISO();

      if (datos.fechaEntrada && datos.fechaEntrada < hoy) {
        ctx.addIssue({
          code: 'custom',
          path: ['fechaEntrada'],
          message: 'La fecha de entrada no puede estar en el pasado',
        });
      }

      if (datos.fechaEntrada && datos.fechaSalida && datos.fechaSalida <= datos.fechaEntrada) {
        ctx.addIssue({
          code: 'custom',
          path: ['fechaSalida'],
          message: 'La fecha de salida debe ser posterior a la de entrada',
        });
      }

      if (1 + datos.acompanantes.length > capacidadMaxima) {
        ctx.addIssue({
          code: 'custom',
          path: ['acompanantes'],
          message: `El grupo supera la capacidad máxima del apartamento (${capacidadMaxima} personas)`,
        });
      }

      const vistos = new Set<string>();
      datos.acompanantes.forEach((acompanante, indice) => {
        const doc = acompanante.documento.trim().toLowerCase();
        if (!doc) return;

        if (doc === docTitularNormalizado) {
          ctx.addIssue({
            code: 'custom',
            path: ['acompanantes', indice, 'documento'],
            message: 'Este documento ya corresponde al titular de la reserva',
          });
          return;
        }

        if (vistos.has(doc)) {
          ctx.addIssue({
            code: 'custom',
            path: ['acompanantes', indice, 'documento'],
            message: 'No se puede repetir el mismo documento en el grupo',
          });
          return;
        }

        vistos.add(doc);
      });
    });
}

export type FormularioReservaHuesped = z.infer<ReturnType<typeof crearEsquemaReservaHuesped>>;
