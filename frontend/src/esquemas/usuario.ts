import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

// Contraseña segura (7.2), igual que PoliticaContrasena del backend: ^(?=.*[A-Z])(?=.*\d).{8,72}$
export const contrasenaSegura = z
  .string()
  .min(8, 'Mínimo 8 caracteres')
  .max(72, 'Máximo 72 caracteres')
  .regex(/[A-Z]/, 'Debe tener al menos una mayúscula')
  .regex(/[0-9]/, 'Debe tener al menos un número');

// Campos de una persona, con los límites de RegistroHuespedRequest y CrearUsuarioRequest.
const camposPersona = {
  nombreCompleto: z.string().trim().min(1, 'Ingrese su nombre completo').max(150, 'Máximo 150 caracteres'),
  correo: z.email('Ingrese un correo electrónico válido').max(120, 'Máximo 120 caracteres'),
  documento: z.string().trim().min(1, 'Ingrese el documento de identidad').max(30, 'Máximo 30 caracteres'),
  telefono: z.string().trim().min(1, 'Ingrese un teléfono de contacto').max(30, 'Máximo 30 caracteres'),
  // @Past del backend: la fecha de nacimiento debe ser anterior a hoy
  fechaNacimiento: z
    .string()
    .min(1, 'Ingrese la fecha de nacimiento')
    .refine((fecha) => fecha < hoyISO(), 'La fecha de nacimiento debe ser anterior a hoy'),
  contrasena: contrasenaSegura,
  confirmacion: z.string().min(1, 'Repita la contraseña'),
};

const contrasenasCoinciden = {
  check: (datos: { contrasena: string; confirmacion: string }) => datos.contrasena === datos.confirmacion,
  mensaje: { path: ['confirmacion'], message: 'Las contraseñas no coinciden' },
};

// Registro público: SIN rol. El backend siempre crea un HUESPED.
export const esquemaRegistro = z
  .object(camposPersona)
  .refine(contrasenasCoinciden.check, contrasenasCoinciden.mensaje);

export type FormularioRegistro = z.infer<typeof esquemaRegistro>;

// Usuario creado por el administrador: él escoge el rol.
export const esquemaCrearUsuario = z
  .object({
    ...camposPersona,
    rol: z.enum(['RECEPCIONISTA', 'PERSONAL_SERVICIO', 'ADMINISTRADOR', 'HUESPED'], 'Seleccione un rol'),
  })
  .refine(contrasenasCoinciden.check, contrasenasCoinciden.mensaje);

export type FormularioCrearUsuario = z.infer<typeof esquemaCrearUsuario>;
