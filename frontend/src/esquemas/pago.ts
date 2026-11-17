import { z } from 'zod';

// Espejo de RegistrarPagoRequest y de la entidad Pago: importe distinto de cero
// (un valor negativo documenta una devolución), medio y concepto de hasta 300 caracteres.
export const esquemaPago = z.object({
  valor: z
    .number({ error: 'Ingrese el valor del pago' })
    .int('Ingrese el valor sin decimales')
    .refine((valor) => valor !== 0, 'El valor no puede ser cero'),
  medio: z.enum(['EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'TRANSFERENCIA_BANCARIA'], 'Seleccione el medio de pago'),
  concepto: z.string().trim().min(1, 'Ingrese el concepto del pago').max(300, 'Máximo 300 caracteres'),
});

export type FormularioPago = z.infer<typeof esquemaPago>;
