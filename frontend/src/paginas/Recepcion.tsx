import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Aviso } from '../componentes/Aviso';
import { Cargando } from '../componentes/Cargando';
import { DialogoConfirmacion } from '../componentes/DialogoConfirmacion';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta } from '../componentes/Etiqueta';
import { EtiquetaEstado } from '../componentes/EtiquetaEstado';
import { Icono } from '../componentes/Icono';
import { fotoDeApartamento, nombreDeApartamento } from '../datos/fotos';
import type { Apartamento } from '../modelos/apartamento';
import type { Folio } from '../modelos/folio';
import type { ReservaDetalle, ReservaResumen } from '../modelos/reserva';
import { listarApartamentos } from '../servicios/apartamentoServicio';
import { consultarFolio } from '../servicios/folioServicio';
import {
  cambiarEstadoReserva,
  consultarMovimientosDelDia,
  consultarReserva,
  listarReservas,
} from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { fechaCorta, fechaLarga, hora, pesos } from '../utilidades/formato';
import { hoyISO } from '../utilidades/fechas';

interface DatosDelDia {
  llegadas: ReservaDetalle[];
  salidas: { reserva: ReservaResumen; folio: Folio }[];
  apartamentos: Apartamento[];
  recientes: ReservaResumen[];
}

type AccionPendiente =
  | { tipo: 'confirmar' | 'registrar-llegada' | 'no-show' | 'registrar-salida'; reserva: { codigo: string; titular: string } }
  | null;

const textoAccion = {
  confirmar: { titulo: 'Confirmar la reserva', boton: 'Confirmar', exito: 'Reserva confirmada.' },
  'registrar-llegada': { titulo: 'Registrar la llegada', boton: 'Registrar llegada', exito: 'Llegada registrada: el apartamento queda ocupado.' },
  'no-show': { titulo: 'Declarar no-show', boton: 'Declarar no-show', exito: 'No-show registrado según la política de la reserva.' },
  'registrar-salida': { titulo: 'Registrar la salida', boton: 'Registrar salida', exito: 'Salida registrada: el apartamento pasa a preparación.' },
};

// Panel de la recepción (7.6): llegadas y salidas previstas de un día, con sus acciones.
export default function Recepcion() {
  const [fecha, setFecha] = useState(hoyISO);
  const [datos, setDatos] = useState<DatosDelDia | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [accion, setAccion] = useState<AccionPendiente>(null);
  const [procesando, setProcesando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [recarga, setRecarga] = useState(0);
  const cerrarAviso = useCallback(() => setAviso(null), []);

  useEffect(() => {
    let vigente = true;
    async function cargar() {
      setDatos(null);
      setError(null);
      try {
        const [movimientos, apartamentos, recientes] = await Promise.all([
          consultarMovimientosDelDia(fecha),
          listarApartamentos(),
          listarReservas({ size: 5 }),
        ]);
        // El resumen no trae la hora de llegada ni el saldo: se consultan el detalle y el folio de cada una.
        const [llegadas, salidas] = await Promise.all([
          Promise.all(movimientos.llegadas.map((r) => consultarReserva(r.codigo))),
          Promise.all(movimientos.salidas.map(async (r) => ({ reserva: r, folio: await consultarFolio(r.codigo) }))),
        ]);
        if (vigente) {
          setDatos({ llegadas, salidas, apartamentos, recientes: recientes.contenido });
        }
      } catch (e) {
        if (vigente) setError(mensajeDeError(e));
      }
    }
    cargar();
    return () => {
      vigente = false;
    };
  }, [fecha, recarga]);

  async function ejecutarAccion() {
    if (!accion) return;
    setProcesando(true);
    try {
      await cambiarEstadoReserva(accion.reserva.codigo, accion.tipo);
      setAviso(`${accion.reserva.codigo}: ${textoAccion[accion.tipo].exito}`);
      setRecarga((n) => n + 1);
    } catch (e) {
      setError(mensajeDeError(e));
    } finally {
      setProcesando(false);
      setAccion(null);
    }
  }

  const esHoy = fecha === hoyISO();
  const ocupados = datos?.apartamentos.filter((a) => a.estadoOperativo === 'OCUPADO').length ?? 0;
  const totalApartamentos = datos?.apartamentos.length ?? 0;
  const pendientesDeConfirmar = datos?.llegadas.filter((r) => r.estado === 'PENDIENTE').length ?? 0;
  const salidasConSaldo = datos?.salidas.filter((s) => s.folio.saldo !== 0).length ?? 0;

  return (
    <section>
      <EncabezadoPagina
        antetitulo={fechaLarga(fecha).toUpperCase()}
        titulo={esHoy ? 'Recepción del día' : 'Movimientos del día'}
        descripcion="Llegadas y salidas previstas: confirmación, registro de llegada, no-show y registro de salida."
      >
        <div className="selector-dia">
          <label htmlFor="dia">Día</label>
          <input id="dia" type="date" value={fecha} onChange={(e) => e.target.value && setFecha(e.target.value)} />
          {!esHoy && (
            <button type="button" className="secondary compact" onClick={() => setFecha(hoyISO())}>
              Hoy
            </button>
          )}
          <Link className="primary" to="/recepcion/nueva-reserva">
            <Icono nombre="mas" /> Nueva reserva directa
          </Link>
        </div>
      </EncabezadoPagina>

      {error && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {error}
        </p>
      )}

      <div className="stat-grid">
        <div className="stat">
          <span className="stat-icon green">
            <Icono nombre="llave" />
          </span>
          <div>
            <small>Llegadas previstas</small>
            <strong>{datos?.llegadas.length ?? '–'}</strong>
            <span>{datos ? `${datos.llegadas.length - pendientesDeConfirmar} confirmadas` : 'Cargando…'}</span>
          </div>
        </div>
        <div className="stat">
          <span className="stat-icon amber">
            <Icono nombre="salir" />
          </span>
          <div>
            <small>Salidas previstas</small>
            <strong>{datos?.salidas.length ?? '–'}</strong>
            <span>{datos ? `${salidasConSaldo} con saldo en el folio` : 'Cargando…'}</span>
          </div>
        </div>
        <div className="stat">
          <span className="stat-icon blue">
            <Icono nombre="cama" />
          </span>
          <div>
            <small>Ocupación actual</small>
            <strong>{totalApartamentos ? `${Math.round((ocupados / totalApartamentos) * 100)}%` : '–'}</strong>
            <span>
              {ocupados} de {totalApartamentos} apartamentos
            </span>
          </div>
        </div>
        <div className="stat">
          <span className="stat-icon red">
            <Icono nombre="alerta" />
          </span>
          <div>
            <small>Requiere atención</small>
            <strong>{datos ? pendientesDeConfirmar + salidasConSaldo : '–'}</strong>
            <span>
              {pendientesDeConfirmar} por confirmar · {salidasConSaldo} con saldo
            </span>
          </div>
        </div>
      </div>

      <div className="daily-grid">
        <section className="task-card" aria-labelledby="titulo-llegadas">
          <div className="card-head">
            <div>
              <span className="eyebrow">LLEGADAS</span>
              <h3 id="titulo-llegadas">Próximas entradas</h3>
            </div>
            {datos && <Etiqueta tono="available">{datos.llegadas.length} previstas</Etiqueta>}
          </div>

          {!datos && !error && <Cargando filas={2} />}
          {datos?.llegadas.length === 0 && <SinMovimientos texto="No hay llegadas previstas para este día." />}
          {datos?.llegadas.map((reserva) => (
            <div className="guest-task" key={reserva.codigo}>
              <span className="time">{hora(reserva.horaEstimadaLlegada)}</span>
              <div className="huesped">
                <img src={fotoDeApartamento(reserva.identificacionApartamento)} alt="" />
                <div>
                  <b>{reserva.titular.nombreCompleto}</b>
                  <span>
                    {reserva.codigo} · {nombreDeApartamento(reserva.identificacionApartamento)} · {reserva.ocupantes.length}{' '}
                    {reserva.ocupantes.length === 1 ? 'persona' : 'personas'}
                  </span>
                </div>
              </div>
              <EtiquetaEstado estado={reserva.estado} />
              <div className="acciones">
                {reserva.estado === 'PENDIENTE' ? (
                  <>
                    <Link className="secondary compact" to={`/recepcion/reservas/${reserva.codigo}/folio`}>
                      Ver folio
                    </Link>
                    <button
                      type="button"
                      className="primary compact"
                      onClick={() => setAccion({ tipo: 'confirmar', reserva: { codigo: reserva.codigo, titular: reserva.titular.nombreCompleto } })}
                    >
                      Confirmar
                    </button>
                  </>
                ) : (
                  <>
                    <button
                      type="button"
                      className="secondary compact peligro"
                      onClick={() => setAccion({ tipo: 'no-show', reserva: { codigo: reserva.codigo, titular: reserva.titular.nombreCompleto } })}
                    >
                      No-show
                    </button>
                    <button
                      type="button"
                      className="primary compact"
                      onClick={() => setAccion({ tipo: 'registrar-llegada', reserva: { codigo: reserva.codigo, titular: reserva.titular.nombreCompleto } })}
                    >
                      Registrar llegada
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
          <p className="hint">
            <Icono nombre="reloj" tamano={16} /> La confirmación exige el anticipo del 30 % en el folio. El no-show se
            habilita desde las 20:00 del día de entrada.
          </p>
        </section>

        <section className="task-card" aria-labelledby="titulo-salidas">
          <div className="card-head">
            <div>
              <span className="eyebrow">SALIDAS</span>
              <h3 id="titulo-salidas">Próximas salidas</h3>
            </div>
            {datos && (
              <Etiqueta tono={salidasConSaldo ? 'warning' : 'available'}>
                {salidasConSaldo ? `${salidasConSaldo} con saldo` : `${datos.salidas.length} previstas`}
              </Etiqueta>
            )}
          </div>

          {!datos && !error && <Cargando filas={1} />}
          {datos?.salidas.length === 0 && <SinMovimientos texto="No hay salidas previstas para este día." />}
          {datos?.salidas.map(({ reserva, folio }) => (
            <div className="checkout-guest" key={reserva.codigo}>
              <div className="checkout-top">
                <img className="miniatura" src={fotoDeApartamento(reserva.identificacionApartamento)} alt="" />
                <div>
                  <b>{reserva.titularNombre}</b>
                  <span>
                    {reserva.codigo} · {nombreDeApartamento(reserva.identificacionApartamento)}
                  </span>
                </div>
              </div>
              {folio.saldo !== 0 ? (
                <div className="balance-alert">
                  <span>
                    <Icono nombre="billetera" /> Saldo del folio
                  </span>
                  <strong>{pesos(folio.saldo)}</strong>
                </div>
              ) : (
                <div className="balance-alert ok">
                  <span>
                    <Icono nombre="chequeo" /> {folio.cerrado ? 'Folio cerrado' : 'Folio en cero: listo para cerrar'}
                  </span>
                </div>
              )}
              <div className="button-row">
                <Link className="secondary compact" to={`/recepcion/reservas/${reserva.codigo}/folio`}>
                  {folio.cerrado ? 'Ver folio' : 'Pagos y cierre'}
                </Link>
                <button
                  type="button"
                  className="primary compact"
                  disabled={!folio.cerrado}
                  title={folio.cerrado ? undefined : 'Primero se cierra el folio'}
                  onClick={() => setAccion({ tipo: 'registrar-salida', reserva: { codigo: reserva.codigo, titular: reserva.titularNombre } })}
                >
                  Registrar salida
                </button>
              </div>
            </div>
          ))}
          <p className="hint">
            <Icono nombre="billetera" tamano={16} /> La salida exige el folio cerrado; con saldo, solo lo cierra el
            administrador.
          </p>
        </section>
      </div>

      <section className="table-card" aria-labelledby="titulo-recientes">
        <div className="card-head">
          <div>
            <span className="eyebrow">INVENTARIO UNIFICADO</span>
            <h3 id="titulo-recientes">Reservas recientes</h3>
          </div>
          <Link className="secondary compact" to="/recepcion/reservas">
            <Icono nombre="filtro" /> Ver todas y filtrar
          </Link>
        </div>
        {!datos && !error && <Cargando filas={3} />}
        {datos && (
          <div className="data-table">
            <div className="tr th">
              <span>CÓDIGO</span>
              <span>TITULAR</span>
              <span>ESTANCIA</span>
              <span>CANAL</span>
              <span>ESTADO</span>
              <span>VALOR</span>
            </div>
            {datos.recientes.map((r) => (
              <Link className="tr fila-enlace" key={r.codigo} to={`/recepcion/reservas/${r.codigo}/folio`}>
                <span>
                  <b>{r.codigo}</b>
                  <small>{nombreDeApartamento(r.identificacionApartamento)}</small>
                </span>
                <span>{r.titularNombre}</span>
                <span>
                  {fechaCorta(r.fechaEntrada)} → {fechaCorta(r.fechaSalida)}
                </span>
                <span>
                  <Etiqueta>{r.canalOrigen}</Etiqueta>
                </span>
                <span>
                  <EtiquetaEstado estado={r.estado} />
                </span>
                <span>
                  <b>{pesos(r.valorTotal)}</b>
                </span>
              </Link>
            ))}
          </div>
        )}
      </section>

      {accion && (
        <DialogoConfirmacion
          titulo={textoAccion[accion.tipo].titulo}
          textoConfirmar={textoAccion[accion.tipo].boton}
          peligroso={accion.tipo === 'no-show'}
          ocupado={procesando}
          onConfirmar={ejecutarAccion}
          onCancelar={() => setAccion(null)}
        >
          <p>
            Reserva <b>{accion.reserva.codigo}</b> de {accion.reserva.titular}.{' '}
            {accion.tipo === 'no-show' && 'Se aplica la política de cancelación congelada en la reserva. No se puede deshacer.'}
            {accion.tipo === 'confirmar' && 'El sistema verifica que el anticipo del 30 % esté pagado.'}
            {accion.tipo === 'registrar-llegada' && 'El apartamento debe estar preparado.'}
            {accion.tipo === 'registrar-salida' && 'La reserva queda finalizada y el apartamento pasa a preparación.'}
          </p>
        </DialogoConfirmacion>
      )}

      {aviso && <Aviso mensaje={aviso} onCerrar={cerrarAviso} />}
    </section>
  );
}

function SinMovimientos({ texto }: { texto: string }) {
  return (
    <div className="sin-movimientos">
      <Icono nombre="luna" tamano={22} />
      <p>{texto}</p>
    </div>
  );
}
