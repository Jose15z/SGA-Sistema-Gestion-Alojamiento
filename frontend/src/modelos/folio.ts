export type MedioPago = 'EFECTIVO' | 'TARJETA_DEBITO' | 'TARJETA_CREDITO' | 'TRANSFERENCIA_BANCARIA';

export type TipoCargo = 'ALOJAMIENTO' | 'AJUSTE_ALOJAMIENTO' | 'REVERSO_ALOJAMIENTO' | 'SERVICIO_ADICIONAL' | 'PENALIDAD';

// Espejo de: record CargoResponse(...)
export interface Cargo {
  tipo: TipoCargo;
  concepto: string;
  valor: number;
  momento: string;
  autor: string;
}

// Espejo de: record PagoResponse(...)
export interface Pago {
  identificacion: string;
  valor: number;     // negativo cuando documenta una devolución
  medio: MedioPago;
  momento: string;
  concepto: string;
  autor: string;
}

// Espejo de: record FolioResponse(...)
export interface Folio {
  codigoReserva: string;
  saldo: number;          // cargos menos pagos (RN-15)
  totalPagado: number;
  cerrado: boolean;
  autorizacionCierre: { autor: string; motivo: string; momento: string } | null;
  cargos: Cargo[];
  pagos: Pago[];
}

// Espejo de: record RegistrarPagoRequest(Long valor, MedioPago medio, String concepto)
export interface RegistrarPagoRequest {
  valor: number;
  medio: MedioPago;
  concepto: string;
}

export const nombreMedioPago: Record<MedioPago, string> = {
  EFECTIVO: 'Efectivo',
  TARJETA_DEBITO: 'Tarjeta débito',
  TARJETA_CREDITO: 'Tarjeta crédito',
  TRANSFERENCIA_BANCARIA: 'Transferencia bancaria',
};

export const nombreTipoCargo: Record<TipoCargo, string> = {
  ALOJAMIENTO: 'Cargo de alojamiento',
  AJUSTE_ALOJAMIENTO: 'Ajuste de alojamiento',
  REVERSO_ALOJAMIENTO: 'Reverso de alojamiento',
  SERVICIO_ADICIONAL: 'Servicio adicional',
  PENALIDAD: 'Penalidad',
};
