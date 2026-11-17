import { zodResolver } from '@hookform/resolvers/zod';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useParams } from 'react-router';
import { Aviso } from '../componentes/Aviso';
import { Cargando } from '../componentes/Cargando';
import { DialogoConfirmacion } from '../componentes/DialogoConfirmacion';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta } from '../componentes/Etiqueta';
import { EtiquetaEstado } from '../componentes/EtiquetaEstado';
import { Icono } from '../componentes/Icono';
import { MovimientosFolio } from '../componentes/MovimientosFolio';
import { fotoDeApartamento, nombreDeApartamento } from '../datos/fotos';
import { esquemaPago, type FormularioPago } from '../esquemas/pago';
import { nombreMedioPago, type Folio as FolioDeReserva, type MedioPago } from '../modelos/folio';
import { nombreAccionHistorial, type ReservaDetalle } from '../modelos/reserva';
import { cerrarFolio, consultarFolio, registrarPago } from '../servicios/folioServicio';
import { cambiarEstadoReserva, cancelarReserva, consultarReserva } from '../servicios/reservaServicio';
import { leerSesion } from '../servicios/sesion';
import { mensajeDeError } from '../utilidades/errores';
import { fechaCorta, fechaHora, hora, noches, pesos } from '../utilidades/formato';

// Porcentaje de anticipo de la Ficha (sga.alojamiento.porcentaje-anticipo en el backend).
// Solo sirve para sugerir el valor: quien decide si alcanza es el backend al confirmar.
const PORCENTAJE_ANTICIPO = 30;

const MEDIOS: MedioPago[] = ['EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'TRANSFERENCIA_BANCARIA'];

type Accion = 'confirmar' | 'registrar-llegada' | 'registrar-salida' | 'no-show' | 'cancelar' | 'cerrar-folio';

const textoAccion: Record<Accion, { titulo: string; boton: string; exito: string; peligroso?: boolean }> = {
  confirmar: { titulo: 'Confirmar la reserva', boton: 'Confirmar', exito: 'Reserva confirmada.' },
  'registrar-llegada': { titulo: 'Registrar la llegada', boton: 'Registrar llegada', exito: 'Llegada registrada.' },
  'registrar-salida': { titulo: 'Registrar la salida', boton: 'Registrar salida', exito: 'Salida registrada.' },
  'no-show': { titulo: 'Declarar no-show', boton: 'Declarar no-show', exito: 'No-show registrado.', peligroso: true },
  cancelar: { titulo: 'Cancelar la reserva', boton: 'Cancelar reserva', exito: 'Reserva cancelada.', peligroso: true },
  'cerrar-folio': { titulo: 'Cerrar el folio', boton: 'Cerrar folio', exito: 'Folio cerrado.' },
};

// Folio completo de una reserva (7.7): movimientos, pagos y las acciones de la recepción.
export default function Folio() {
  const { codigo = '' } = useParams();
  const ubicacion = useLocation();
  const esAdministrador = leerSesion()?.rol === 'ADMINISTRADOR';

  const [datos, setDatos] = useState<{ reserva: ReservaDetalle; folio: FolioDeReserva } | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [recarga, setRecarga] = useState(0);
  const [aviso, setAviso] = useState<string | null>(
    (ubicacion.state as { aviso?: string } | null)?.aviso ?? null,
  );
  const [accion, setAccion] = useState<Accion | null>(null);
  const [motivo, setMotivo] = useState('');
  const [procesando, setProcesando] = useState(false);
  const cerrarAviso = useCallback(() => setAviso(null), []);

  useEffect(() => {
    let vigente = true;
    Promise.all([consultarReserva(codigo), consultarFolio(codigo)])
      .then(([reserva, folio]) => {
        if (vigente) {
          setDatos({ reserva, folio });
          setError(null);
        }
      })
      .catch((e) => vigente && setError(mensajeDeError(e)));
    return () => {
      vigente = false;
    };
  }, [codigo, recarga]);

  const {
    register,
    handleSubmit,
    setValue,
    reset,
    setError: setErrorPago,
    formState: { errors, isSubmitting },
  } = useForm<FormularioPago>({
    resolver: zodResolver(esquemaPago),
    mode: 'onTouched',
    defaultValues: { medio: 'EFECTIVO', concepto: '' },
  });

  async function alRegistrarPago(pago: FormularioPago) {
    try {
      await registrarPago(codigo, pago);
      reset({ medio: pago.medio, concepto: '' });
      setAviso(`Pago de ${pesos(pago.valor)} registrado en ${nombreMedioPago[pago.medio].toLowerCase()}.`);
      setRecarga((n) => n + 1);
    } catch (e) {
      setErrorPago('root.servidor', { message: mensajeDeError(e) });
    }
  }

  async function ejecutarAccion() {
    if (!accion || !datos) return;
    const requiereMotivo = accion === 'cancelar' || (accion === 'cerrar-folio' && datos.folio.saldo !== 0);
    if (requiereMotivo && !motivo.trim()) return;
    setProcesando(true);
    try {
      if (accion === 'cancelar') {
        await cancelarReserva(codigo, motivo.trim());
      } else if (accion === 'cerrar-folio') {
        await cerrarFolio(codigo, datos.folio.saldo !== 0 ? motivo.trim() : null);
      } else {
        await cambiarEstadoReserva(codigo, accion);
      }
      setAviso(textoAccion[accion].exito);
      setRecarga((n) => n + 1);
    } catch (e) {
      setError(mensajeDeError(e));
    } finally {
      setProcesando(false);
      setAccion(null);
      setMotivo('');
    }
  }

  if (!datos) {
    return (
      <section>
        <EncabezadoPagina antetitulo="FOLIO DE RESERVA" titulo={codigo} descripcion="Consultando la reserva y su folio…" />
        {error ? (
          <p className="form-error bloque" role="alert">
            <Icono nombre="alerta" tamano={16} /> {error}
          </p>
        ) : (
          <Cargando filas={5} />
        )}
      </section>
    );
  }

  const { reserva, folio } = datos;
  const valorReserva = reserva.cotizacionCongelada.valorTotal;
  const anticipo = Math.ceil((valorReserva * PORCENTAJE_ANTICIPO) / 100);
  const faltaAnticipo = Math.max(anticipo - folio.totalPagado, 0);
  const activa = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO'].includes(reserva.estado);
  const cierreRequiereAdministrador = folio.saldo !== 0 && !esAdministrador;
  const requiereMotivo = accion === 'cancelar' || (accion === 'cerrar-folio' && folio.saldo !== 0);

  return (
    <section>
      <EncabezadoPagina
        antetitulo={`FOLIO DE RESERVA · CANAL ${reserva.canalOrigen}`}
        titulo={reserva.codigo}
        descripcion={`${nombreDeApartamento(reserva.identificacionApartamento)} · ${reserva.titular.nombreCompleto}`}
      >
        <div className="button-row">
          <Link className="secondary" to="/recepcion/reservas">
            Volver a reservas
          </Link>
          <Link className="secondary" to="/recepcion">
            Recepción del día
          </Link>
        </div>
      </EncabezadoPagina>

      {error && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {error}
        </p>
      )}

      <div className="reservation-layout folio-pagina">
        <div className="booking-form-main">
          <section className="form-section tarjeta-reserva" aria-labelledby="titulo-reserva">
            <img src={fotoDeApartamento(reserva.identificacionApartamento)} alt={`Apartamento ${reserva.identificacionApartamento}`} />
            <div>
              <div className="folio-head">
                <div>
                  <span className="eyebrow">RESERVA</span>
                  <h3 id="titulo-reserva">{nombreDeApartamento(reserva.identificacionApartamento)}</h3>
                </div>
                <EtiquetaEstado estado={reserva.estado} />
              </div>
              <div className="folio-summary">
                <div>
                  <small>Estancia</small>
                  <b>
                    {fechaCorta(reserva.fechaEntrada)} → {fechaCorta(reserva.fechaSalida)} ·{' '}
                    {noches(reserva.fechaEntrada, reserva.fechaSalida)} noches
                  </b>
                </div>
                <div>
                  <small>Llegada estimada</small>
                  <b>{hora(reserva.horaEstimadaLlegada)}</b>
                </div>
                <div>
                  <small>Valor congelado</small>
                  <b>{pesos(valorReserva)}</b>
                </div>
                <div>
                  <small>Creada</small>
                  <b>{fechaHora(reserva.fechaCreacion)}</b>
                </div>
              </div>
            </div>
          </section>

          <section className="form-section" aria-labelledby="titulo-ocupantes">
            <div className="card-head">
              <div>
                <span className="eyebrow">GRUPO</span>
                <h3 id="titulo-ocupantes">Ocupantes · {reserva.ocupantes.length}</h3>
              </div>
            </div>
            <ul className="lista-ocupantes">
              {reserva.ocupantes.map((o) => (
                <li key={o.documento}>
                  <span className="avatar">{o.nombreCompleto.slice(0, 2).toUpperCase()}</span>
                  <span>
                    <b>{o.nombreCompleto}</b>
                    <small>
                      Documento {o.documento} · nacido el {fechaCorta(o.fechaNacimiento)} {o.fechaNacimiento.slice(0, 4)}
                    </small>
                  </span>
                  {o.documento === reserva.titular.documento && <Etiqueta tono="confirmed">Titular</Etiqueta>}
                </li>
              ))}
            </ul>
          </section>

          <section className="form-section" aria-labelledby="titulo-historial">
            <div className="card-head">
              <div>
                <span className="eyebrow">TRAZABILIDAD</span>
                <h3 id="titulo-historial">Historial de la reserva</h3>
              </div>
            </div>
            <ol className="historial">
              {reserva.historial.map((evento, i) => (
                <li key={i}>
                  <span className="punto" />
                  <div>
                    <b>{nombreAccionHistorial[evento.accion] ?? evento.accion}</b>
                    <small>
                      {fechaHora(evento.momento)} · {evento.autor}
                      {evento.observacion ? ` · ${evento.observacion}` : ''}
                    </small>
                  </div>
                  {evento.estadoNuevo && <EtiquetaEstado estado={evento.estadoNuevo} />}
                </li>
              ))}
            </ol>
          </section>
        </div>

        <aside className="folio-card" aria-labelledby="titulo-folio">
          <div className="folio-head">
            <div>
              <span className="eyebrow">FOLIO</span>
              <h3 id="titulo-folio">{folio.cerrado ? 'Folio cerrado' : 'Folio abierto'}</h3>
            </div>
            <Etiqueta tono={folio.cerrado ? 'neutral' : 'available'}>{folio.cerrado ? 'CERRADO' : 'ABIERTO'}</Etiqueta>
          </div>

          <MovimientosFolio folio={folio} />

          {folio.autorizacionCierre && (
            <p className="policy-note">
              <Icono nombre="escudo" tamano={14} /> Cierre con saldo autorizado por {folio.autorizacionCierre.autor}:{' '}
              {folio.autorizacionCierre.motivo}
            </p>
          )}

          {!folio.cerrado && (
            <form className="formulario-pago" onSubmit={handleSubmit(alRegistrarPago)} noValidate>
              <h4>Registrar pago</h4>
              {reserva.estado === 'PENDIENTE' && faltaAnticipo > 0 && (
                <button
                  type="button"
                  className="sugerencia"
                  onClick={() => {
                    setValue('valor', faltaAnticipo, { shouldValidate: true });
                    setValue('concepto', `Anticipo del ${PORCENTAJE_ANTICIPO} %`, { shouldValidate: true });
                  }}
                >
                  <Icono nombre="destello" tamano={14} /> Anticipo del {PORCENTAJE_ANTICIPO} %: faltan {pesos(faltaAnticipo)}
                </button>
              )}
              {folio.saldo > 0 && reserva.estado !== 'PENDIENTE' && (
                <button
                  type="button"
                  className="sugerencia"
                  onClick={() => {
                    setValue('valor', folio.saldo, { shouldValidate: true });
                    setValue('concepto', 'Pago del saldo', { shouldValidate: true });
                  }}
                >
                  <Icono nombre="destello" tamano={14} /> Pagar el saldo: {pesos(folio.saldo)}
                </button>
              )}
              <label htmlFor="valor">
                VALOR (COP)
                <input
                  id="valor"
                  type="number"
                  inputMode="numeric"
                  step={1}
                  placeholder="0"
                  aria-invalid={errors.valor ? true : undefined}
                  {...register('valor', { valueAsNumber: true })}
                />
                {errors.valor && <span className="error">{errors.valor.message}</span>}
              </label>
              <label htmlFor="medio">
                MEDIO DE PAGO
                <select id="medio" {...register('medio')}>
                  {MEDIOS.map((m) => (
                    <option key={m} value={m}>
                      {nombreMedioPago[m]}
                    </option>
                  ))}
                </select>
              </label>
              <label htmlFor="concepto">
                CONCEPTO
                <input
                  id="concepto"
                  placeholder="Ej. Anticipo por transferencia"
                  aria-invalid={errors.concepto ? true : undefined}
                  {...register('concepto')}
                />
                {errors.concepto && <span className="error">{errors.concepto.message}</span>}
              </label>
              {errors.root?.servidor && (
                <p className="form-error" role="alert">
                  <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
                </p>
              )}
              <button type="submit" className="primary full" disabled={isSubmitting}>
                <Icono nombre="tarjeta" /> {isSubmitting ? 'Registrando…' : 'Registrar pago'}
              </button>
            </form>
          )}

          <div className="acciones-folio">
            <h4>Acciones</h4>
            {reserva.estado === 'PENDIENTE' && (
              <button type="button" className="primary full" onClick={() => setAccion('confirmar')}>
                <Icono nombre="chequeo" /> Confirmar reserva
              </button>
            )}
            {reserva.estado === 'CONFIRMADA' && (
              <>
                <button type="button" className="primary full" onClick={() => setAccion('registrar-llegada')}>
                  <Icono nombre="llave" /> Registrar llegada (check-in)
                </button>
                <button type="button" className="secondary full peligro" onClick={() => setAccion('no-show')}>
                  Declarar no-show
                </button>
              </>
            )}
            {!folio.cerrado && (
              <button
                type="button"
                className="secondary full"
                onClick={() => setAccion('cerrar-folio')}
                disabled={cierreRequiereAdministrador}
                aria-describedby={cierreRequiereAdministrador ? 'nota-cierre' : undefined}
              >
                <Icono nombre="billetera" /> Cerrar folio
              </button>
            )}
            {cierreRequiereAdministrador && !folio.cerrado && (
              <p className="hint" id="nota-cierre">
                <Icono nombre="escudo" tamano={14} /> Con saldo distinto de cero, solo el administrador autoriza el cierre.
              </p>
            )}
            {reserva.estado === 'EN_CURSO' && (
              <button
                type="button"
                className="primary full"
                onClick={() => setAccion('registrar-salida')}
                disabled={!folio.cerrado}
                aria-describedby={folio.cerrado ? undefined : 'nota-salida'}
              >
                <Icono nombre="salir" /> Registrar salida (check-out)
              </button>
            )}
            {reserva.estado === 'EN_CURSO' && !folio.cerrado && (
              <p className="hint" id="nota-salida">
                <Icono nombre="reloj" tamano={14} /> La salida exige el folio cerrado.
              </p>
            )}
            {(reserva.estado === 'PENDIENTE' || reserva.estado === 'CONFIRMADA') && (
              <button type="button" className="danger-link" onClick={() => setAccion('cancelar')}>
                Cancelar según la política congelada
              </button>
            )}
            {!activa && <p className="hint">La reserva está {reserva.estado.toLowerCase().replace('_', ' ')}: no tiene acciones pendientes.</p>}
          </div>
          <p className="policy-note">
            <Icono nombre="escudo" tamano={14} /> Política de cancelación versión {reserva.versionPolitica}, congelada al crear
            la reserva.
          </p>
        </aside>
      </div>

      {accion && (
        <DialogoConfirmacion
          titulo={textoAccion[accion].titulo}
          textoConfirmar={textoAccion[accion].boton}
          peligroso={textoAccion[accion].peligroso}
          ocupado={procesando}
          confirmarDeshabilitado={requiereMotivo && !motivo.trim()}
          onConfirmar={ejecutarAccion}
          onCancelar={() => {
            setAccion(null);
            setMotivo('');
          }}
        >
          <p>
            {accion === 'confirmar' && 'El backend verifica que el anticipo del 30 % esté pagado y que la hora de llegada esté registrada.'}
            {accion === 'registrar-llegada' && 'El apartamento debe estar preparado. Queda ocupado y la reserva en curso.'}
            {accion === 'registrar-salida' && 'La reserva queda finalizada y el apartamento pasa a preparación.'}
            {accion === 'no-show' && 'Se aplica la política congelada en la reserva. Se habilita desde las 20:00 del día de entrada y no se puede deshacer.'}
            {accion === 'cancelar' && 'Se reversa el alojamiento y se cobra la penalidad que indique la política congelada. No se puede deshacer.'}
            {accion === 'cerrar-folio' &&
              (folio.saldo === 0
                ? 'El saldo está en cero: el folio se cierra y la salida queda habilitada.'
                : `El folio tiene saldo de ${pesos(folio.saldo)}. Su autorización queda registrada con su nombre y el motivo.`)}
          </p>
          {requiereMotivo && (
            <label htmlFor="motivo">
              MOTIVO
              <textarea
                id="motivo"
                rows={3}
                maxLength={accion === 'cancelar' ? 300 : 200}
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
                placeholder={accion === 'cancelar' ? 'Ej. El huésped cambió sus planes de viaje' : 'Ej. Saldo pendiente pagado por transferencia al salir'}
              />
            </label>
          )}
        </DialogoConfirmacion>
      )}

      {aviso && <Aviso mensaje={aviso} onCerrar={cerrarAviso} />}
    </section>
  );
}
