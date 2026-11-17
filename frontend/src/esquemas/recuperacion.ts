import { z } from 'zod';
import { contrasenaSegura } from './usuario';

export const esquemaSolicitud = z.object({
  correo: z.email('Ingrese un correo electrónico válido'),
});

export type FormularioSolicitud = z.infer<typeof esquemaSolicitud>;

// El código llega por correo (en desarrollo, a la consola del backend) y vence a los 15 minutos.
export const esquemaRestablecer = z
  .object({
    codigo: z.string().regex(/^\d{6}$/, 'El código tiene 6 dígitos'),
    contrasenaNueva: contrasenaSegura,
    confirmacion: z.string().min(1, 'Repita la contraseña'),
  })
  .refine((datos) => datos.contrasenaNueva === datos.confirmacion, {
    path: ['confirmacion'],
    message: 'Las contraseñas no coinciden',
  });

export type FormularioRestablecer = z.infer<typeof esquemaRestablecer>;
