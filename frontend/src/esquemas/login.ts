import { z } from 'zod';

// Las reglas del FORMULARIO: forma del mensaje, no reglas del negocio.
// Al ingresar solo se exige que la contraseña no esté vacía; las reglas de una
// contraseña segura se validan al registrarse.
export const esquemaLogin = z.object({
  correo: z.email('Ingrese un correo electrónico válido'),
  contrasena: z.string().min(1, 'Ingrese su contraseña'),
  recordar: z.boolean(),
});

// El tipo del formulario se DEDUCE del esquema: una sola fuente de verdad.
export type FormularioLogin = z.infer<typeof esquemaLogin>;
