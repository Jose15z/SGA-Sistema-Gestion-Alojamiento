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
