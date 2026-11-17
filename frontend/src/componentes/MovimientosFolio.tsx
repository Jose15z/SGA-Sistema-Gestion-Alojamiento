import { nombreMedioPago, nombreTipoCargo, type Folio, type TipoCargo } from '../modelos/folio';
import { fechaHora, pesos } from '../utilidades/formato';
import { Icono, type NombreIcono } from './Icono';

const iconoPorCargo: Record<TipoCargo, NombreIcono> = {
  ALOJAMIENTO: 'luna',
  AJUSTE_ALOJAMIENTO: 'luna',
  REVERSO_ALOJAMIENTO: 'flecha',
  SERVICIO_ADICIONAL: 'destello',
  PENALIDAD: 'alerta',
};

interface Movimiento {
  clave: string;
  esPago: boolean;
  icono: NombreIcono;
  titulo: string;
  detalle: string;
  valor: number;
  momento: string;
}

// Cargos y pagos del folio en orden cronológico, con sus totales (RN-15: el saldo se deriva).
export function MovimientosFolio({ folio }: { folio: Folio }) {
  const movimientos: Movimiento[] = [
    ...folio.cargos.map((c, i) => ({
      clave: `c${i}`,
      esPago: false,
      icono: iconoPorCargo[c.tipo],
      titulo: nombreTipoCargo[c.tipo],
      detalle: `${c.concepto} · ${fechaHora(c.momento)}`,
      valor: c.valor,
      momento: c.momento,
    })),
    ...folio.pagos.map((p) => ({
      clave: p.identificacion,
      esPago: true,
      icono: 'tarjeta' as NombreIcono,
      titulo: p.valor < 0 ? 'Devolución' : `Pago · ${nombreMedioPago[p.medio]}`,
      detalle: `${p.concepto} · ${fechaHora(p.momento)} · ${p.autor}`,
      valor: p.valor,
      momento: p.momento,
    })),
  ].sort((a, b) => a.momento.localeCompare(b.momento));

  const totalCargos = folio.cargos.reduce((suma, c) => suma + c.valor, 0);

  return (
    <>
      <h4>Movimientos</h4>
      {movimientos.length === 0 && <p className="hint">El folio no tiene movimientos.</p>}
      <ul className="lista-movimientos">
        {movimientos.map((m) => (
          <li className={`movement ${m.esPago ? 'payment' : ''}`} key={m.clave}>
            <span className="movement-icon">
              <Icono nombre={m.icono} tamano={16} />
            </span>
            <span>
              <b>{m.titulo}</b>
              <small>{m.detalle}</small>
            </span>
            <strong>{m.esPago ? `− ${pesos(m.valor)}` : pesos(m.valor)}</strong>
          </li>
        ))}
      </ul>
      <div className="folio-total">
        <div>
          <span>Total cargos</span>
          <b>{pesos(totalCargos)}</b>
        </div>
        <div>
          <span>Total pagos</span>
          <b>{pesos(folio.totalPagado)}</b>
        </div>
        <div className="balance">
          <span>{folio.saldo < 0 ? 'Saldo a favor del huésped' : 'Saldo pendiente'}</span>
          <strong>{pesos(Math.abs(folio.saldo))}</strong>
        </div>
      </div>
    </>
  );
}
