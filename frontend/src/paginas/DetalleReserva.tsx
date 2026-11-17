import { zodResolver } from '@hookform/resolvers/zod';
import axios from 'axios';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useParams } from 'react-router';
import { Aviso } from '../componentes/Aviso';
import { Cargando } from '../componentes/Cargando';
import { DialogoCancelarReserva } from '../componentes/DialogoCancelarReserva';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta } from '../componentes/Etiqueta';
import { EtiquetaEstado } from '../componentes/EtiquetaEstado';
import { Icono } from '../componentes/Icono';
import { MovimientosFolio } from '../componentes/MovimientosFolio';
import { fotoDeApartamento, nombreDeApartamento } from '../datos/fotos';
import {
  esquemaHoraLlegada,
  esquemaNovedadLlegada,
  esquemaRegistrarVehiculo,
  type FormularioHoraLlegada,
  type FormularioNovedadLlegada,
  type FormularioRegistrarVehiculo,
} from '../esquemas/reserva';
import type { ErrorResponse } from '../modelos/error';
import type { Folio } from '../modelos/folio';
import { nombreAccionHistorial, type ReservaDetalle } from '../modelos/reserva';
import { consultarFolio } from '../servicios/folioServicio';
import {
  cancelarReserva,
  consultarReserva,
  indicarHoraLlegada,
  informarNovedadLlegada,
  registrarVehiculo,
} from '../servicios/reservaServicio';
import { borrarSesion, leerSesion } from '../servicios/sesion';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';
import { fechaCorta, fechaHora, hora, noches, pesos } from '../utilidades/formato';
import { recordarCodigoReserva } from '../utilidades/misReservasHuesped';
import { consultarMiPerfil } from '../servicios/usuarioServicio';

const ESTADOS_ACTIVOS = new Set(['PENDIENTE', 'CONFIRMADA', 'EN_CURSO']);
const MAX_VEHICULOS = 2;
const PATRON_CODIGO = /^RES-[0-9]{4}-[0-9]{5}$/;

function formatoErrorBackend(error: unknown): string {
  const base = mensajeDeError(error);
  if (axios.isAxiosError<ErrorResponse>(error) && error.response?.data?.detalles?.length) {
    return `${base} (${error.response.data.detalles.join(' · ')})`;
  }
  return base;
}

export default function DetalleReserva() {
  const { codigo = '' } = useParams();
  const codigoNormalizado = codigo.trim().toUpperCase();
  const codigoValido = PATRON_CODIGO.test(codigoNormalizado);

  const [recarga, setRecarga] = useState(0);
  const claveCarga = `${codigoNormalizado}|${recarga}`;
  const [estadoDatos, setEstadoDatos] = useState<{
    clave: string;
    reserva: ReservaDetalle | null;
    folio: Folio | null;
    error: string | null;
    sesionExpirada: boolean;
  } | null>(null);

  const [aviso, setAviso] = useState<string | null>(null);
  const cerrarAviso = useCallback(() => setAviso(null), []);

  useEffect(() => {
    if (!codigoValido) return;
    let vigente = true;

    Promise.all([consultarReserva(codigoNormalizado), consultarFolio(codigoNormalizado)])
      .then(([reserva, folio]) => {
        if (!vigente) return;
        setEstadoDatos({
          clave: claveCarga,
          reserva,
          folio,
          error: null,
          sesionExpirada: false,
        });

        // Si el huésped abrió esta ruta directamente por enlace/URL, se recuerda el código para su sesión.
        if (leerSesion()?.rol === 'HUESPED') {
          consultarMiPerfil()
            .then((perfil) => {
              if (vigente && reserva.titular.documento === perfil.documento) {
                recordarCodigoReserva(perfil.documento, reserva.codigo);
              }
            })
            .catch(() => {
              // No interrumpe la vista si falla el recordatorio opcional.
            });
        }
      })
      .catch((error) => {
        if (!vigente) return;
        const status = estadoHttp(error);
        const expirada = status === 401;
        if (expirada) borrarSesion();
        const mensaje = expirada
          ? 'Su sesión ha expirado o no es válida. Inicie sesión nuevamente para ver esta reserva.'
          : status === 403
            ? `No tiene permisos para ver la reserva ${codigoNormalizado} o no pertenece a su cuenta.`
            : status === 404
              ? `No existe la reserva ${codigoNormalizado}.`
              : formatoErrorBackend(error);
        setEstadoDatos({
          clave: claveCarga,
          reserva: null,
          folio: null,
          error: mensaje,
          sesionExpirada: expirada,
        });
      });

    return () => {
      vigente = false;
    };
  }, [codigoNormalizado, codigoValido, claveCarga]);

  if (!codigoValido) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="MI CUENTA"
          titulo="Código de reserva inválido"
          descripcion="El formato esperado es RES-AAAA-NNNNN (por ejemplo RES-2026-00001)."
        >
          <Link className="secondary" to="/mis-reservas">
            Volver a Mis reservas
          </Link>
        </EncabezadoPagina>
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> El código «{codigo}» no tiene un formato válido.
        </p>
      </section>
    );
  }

  const vigente = estadoDatos?.clave === claveCarga ? estadoDatos : null;

  if (!vigente) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="MI CUENTA"
          titulo={codigoNormalizado}
          descripcion="Consultando detalle de la reserva y su folio…"
        >
          <Link className="secondary" to="/mis-reservas">
            Volver a Mis reservas
          </Link>
        </EncabezadoPagina>
        <Cargando filas={5} texto="Cargando reserva y folio…" />
      </section>
    );
  }

  if (vigente.sesionExpirada) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="SESIÓN REQUERIDA"
          titulo={codigoNormalizado}
          descripcion="Su sesión venció antes de consultar la reserva."
        />
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {vigente.error}
        </p>
        <Link className="primary compact" to="/login" state={{ desde: `/mis-reservas/${codigoNormalizado}` }}>
          Ir a ingresar
        </Link>
      </section>
    );
  }

  if (vigente.error || !vigente.reserva || !vigente.folio) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="MI CUENTA"
          titulo={codigoNormalizado}
          descripcion="No fue posible acceder al detalle de la reserva."
        >
          <Link className="secondary" to="/mis-reservas">
            Volver a Mis reservas
          </Link>
        </EncabezadoPagina>
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {vigente.error ?? 'Error al cargar la reserva.'}
        </p>
        <button type="button" className="secondary compact" onClick={() => setRecarga((n) => n + 1)}>
          Reintentar
        </button>
      </section>
    );
  }

  return (
    <>
      <ContenidoDetalleReserva
        key={`${vigente.reserva.codigo}-${recarga}`}
        reserva={vigente.reserva}
        folio={vigente.folio}
        onActualizado={(mensaje) => {
          setAviso(mensaje);
          setRecarga((n) => n + 1);
        }}
        onSesionExpirada={() => setRecarga((n) => n + 1)}
      />
      {aviso && <Aviso mensaje={aviso} onCerrar={cerrarAviso} />}
    </>
  );
}

interface ContenidoDetalleReservaProps {
  reserva: ReservaDetalle;
  folio: Folio;
  onActualizado: (mensaje: string) => void;
  onSesionExpirada: () => void;
}

function ContenidoDetalleReserva({
  reserva,
  folio,
  onActualizado,
  onSesionExpirada,
}: ContenidoDetalleReservaProps) {
  const [mostrarCancelar, setMostrarCancelar] = useState(false);
  const [procesandoCancelacion, setProcesandoCancelacion] = useState(false);
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null);

  const activa = ESTADOS_ACTIVOS.has(reserva.estado);
  const esConfirmada = reserva.estado === 'CONFIRMADA';
  const puedeCancelar =
    (reserva.estado === 'PENDIENTE' || reserva.estado === 'CONFIRMADA') && !folio.cerrado;
  const puedeRegistrarVehiculo = activa && !folio.cerrado && reserva.vehiculos.length < MAX_VEHICULOS;

  const formHora = useForm<FormularioHoraLlegada>({
    resolver: zodResolver(esquemaHoraLlegada),
    mode: 'onTouched',
    defaultValues: {
      horaEstimadaLlegada: reserva.horaEstimadaLlegada ? reserva.horaEstimadaLlegada.slice(0, 5) : '',
    },
  });

  const formNovedad = useForm<FormularioNovedadLlegada>({
    resolver: zodResolver(esquemaNovedadLlegada),
    mode: 'onTouched',
    defaultValues: { novedad: '' },
  });

  const formVehiculo = useForm<FormularioRegistrarVehiculo>({
    resolver: zodResolver(esquemaRegistrarVehiculo),
    mode: 'onTouched',
    defaultValues: { placa: '' },
  });

  async function alGuardarHora(datos: FormularioHoraLlegada) {
    try {
      await indicarHoraLlegada(reserva.codigo, datos.horaEstimadaLlegada);
      onActualizado(`Hora estimada de llegada actualizada a ${datos.horaEstimadaLlegada}.`);
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        onSesionExpirada();
        return;
      }
      formHora.setError('horaEstimadaLlegada', { message: formatoErrorBackend(error) });
    }
  }

  async function alInformarNovedad(datos: FormularioNovedadLlegada) {
    try {
      await informarNovedadLlegada(reserva.codigo, datos.novedad);
      formNovedad.reset({ novedad: '' });
      onActualizado('Novedad de llegada registrada en la reserva.');
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        onSesionExpirada();
        return;
      }
      formNovedad.setError('novedad', { message: formatoErrorBackend(error) });
    }
  }

  async function alRegistrarVehiculo(datos: FormularioRegistrarVehiculo) {
    try {
      await registrarVehiculo(reserva.codigo, datos.placa);
      formVehiculo.reset({ placa: '' });
      onActualizado(`Vehículo ${datos.placa} registrado en la reserva.`);
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        onSesionExpirada();
        return;
      }
      formVehiculo.setError('placa', { message: formatoErrorBackend(error) });
    }
  }

  async function ejecutarCancelacion(motivo: string) {
    setProcesandoCancelacion(true);
    setErrorGeneral(null);
    try {
      await cancelarReserva(reserva.codigo, motivo);
      setMostrarCancelar(false);
      onActualizado(`Reserva ${reserva.codigo} cancelada según política v${reserva.versionPolitica}.`);
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        onSesionExpirada();
      } else {
        setErrorGeneral(formatoErrorBackend(error));
      }
      setMostrarCancelar(false);
    } finally {
      setProcesandoCancelacion(false);
    }
  }

  const valorReserva = reserva.cotizacionCongelada.valorTotal;

  return (
    <section>
      <EncabezadoPagina
        antetitulo={`MI RESERVA · CANAL ${reserva.canalOrigen}`}
        titulo={reserva.codigo}
        descripcion={`${nombreDeApartamento(reserva.identificacionApartamento)} · Titular ${reserva.titular.nombreCompleto}`}
      >
        <div className="button-row">
          <Link className="secondary" to="/mis-reservas">
            Volver a Mis reservas
          </Link>
        </div>
      </EncabezadoPagina>

      {errorGeneral && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {errorGeneral}
        </p>
      )}

      <div className="reservation-layout folio-pagina">
        <div className="booking-form-main">
          <section className="form-section tarjeta-reserva" aria-labelledby="titulo-reserva">
            <img
              src={fotoDeApartamento(reserva.identificacionApartamento)}
              alt={`Apartamento ${reserva.identificacionApartamento}`}
            />
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
                  <small>Mascotas · Vehículos</small>
                  <b>
                    {reserva.mascotas} · {reserva.vehiculos.length} / {MAX_VEHICULOS}
                  </b>
                </div>
                <div>
                  <small>Creada</small>
                  <b>{fechaHora(reserva.fechaCreacion)}</b>
                </div>
                <div>
                  <small>Política congelada</small>
                  <b>Versión {reserva.versionPolitica}</b>
                </div>
              </div>

              {reserva.novedadLlegada && (
                <p className="form-message" style={{ marginTop: '10px', marginBottom: 0 }}>
                  <Icono nombre="reloj" tamano={14} /> Novedad de llegada reportada: {reserva.novedadLlegada}
                </p>
              )}
            </div>
          </section>

          <section className="form-section" aria-labelledby="titulo-hora-llegada">
            <div className="card-head">
              <div>
                <span className="eyebrow">LLEGADA ESTIMADA (RN-09)</span>
                <h3 id="titulo-hora-llegada">Hora estimada de llegada</h3>
              </div>
              <Etiqueta>{hora(reserva.horaEstimadaLlegada)}</Etiqueta>
            </div>

            {activa ? (
              <form
                onSubmit={formHora.handleSubmit(alGuardarHora)}
                noValidate
                style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', alignItems: 'flex-end' }}
              >
                <label
                  htmlFor="hora-llegada-detalle"
                  style={{ display: 'grid', gap: '6px', fontSize: '9px', fontWeight: 800, color: '#53615a' }}
                >
                  HORA ESTIMADA (HH:MM)
                  <input
                    id="hora-llegada-detalle"
                    type="time"
                    aria-invalid={formHora.formState.errors.horaEstimadaLlegada ? true : undefined}
                    style={{
                      height: '40px',
                      padding: '0 11px',
                      border: '1px solid #d5dcd8',
                      borderRadius: '8px',
                      fontSize: '11px',
                    }}
                    {...formHora.register('horaEstimadaLlegada')}
                  />
                </label>
                <button
                  type="submit"
                  className="primary compact"
                  disabled={formHora.formState.isSubmitting}
                >
                  <Icono nombre="reloj" tamano={14} />{' '}
                  {formHora.formState.isSubmitting ? 'Guardando…' : 'Guardar hora'}
                </button>
                {formHora.formState.errors.horaEstimadaLlegada && (
                  <p className="error" role="alert" style={{ flexBasis: '100%' }}>
                    {formHora.formState.errors.horaEstimadaLlegada.message}
                  </p>
                )}
              </form>
            ) : (
              <p className="hint" style={{ marginTop: 0 }}>
                <Icono nombre="reloj" tamano={14} /> La reserva está en estado terminal y no admite modificar la hora de
                llegada.
              </p>
            )}
          </section>

          <section className="form-section" aria-labelledby="titulo-novedad">
            <div className="card-head">
              <div>
                <span className="eyebrow">AVISO A RECEPCIÓN (REGLA L-15)</span>
                <h3 id="titulo-novedad">Novedad de llegada</h3>
              </div>
              <Etiqueta tono={esConfirmada ? 'confirmed' : 'neutral'}>
                {esConfirmada ? 'Habilitada' : 'Solo en CONFIRMADA'}
              </Etiqueta>
            </div>

            {esConfirmada ? (
              <form
                onSubmit={formNovedad.handleSubmit(alInformarNovedad)}
                noValidate
                style={{ display: 'grid', gap: '10px' }}
              >
                <p style={{ fontSize: '11px', color: 'var(--muted)' }}>
                  Informe un retraso o eventualidad antes de la hora límite de entrada (20:00) para evitar la
                  declaración automática de no-show mientras recepción la atiende.
                </p>
                <label
                  htmlFor="novedad-llegada"
                  style={{ display: 'grid', gap: '6px', fontSize: '9px', fontWeight: 800, color: '#53615a' }}
                >
                  DESCRIPCIÓN DE LA NOVEDAD
                  <textarea
                    id="novedad-llegada"
                    rows={2}
                    maxLength={300}
                    placeholder="Ej. Retraso por cierre vial, llegaremos a las 21:00"
                    aria-invalid={formNovedad.formState.errors.novedad ? true : undefined}
                    style={{
                      padding: '8px 11px',
                      border: '1px solid #d5dcd8',
                      borderRadius: '8px',
                      fontSize: '11px',
                    }}
                    {...formNovedad.register('novedad')}
                  />
                  {formNovedad.formState.errors.novedad && (
                    <span className="error" role="alert">
                      {formNovedad.formState.errors.novedad.message}
                    </span>
                  )}
                </label>
                <div className="button-row" style={{ justifyContent: 'flex-start' }}>
                  <button
                    type="submit"
                    className="secondary compact"
                    disabled={formNovedad.formState.isSubmitting}
                  >
                    <Icono nombre="alerta" tamano={14} />{' '}
                    {formNovedad.formState.isSubmitting ? 'Enviando…' : 'Informar novedad'}
                  </button>
                </div>
              </form>
            ) : (
              <p className="hint" style={{ marginTop: 0 }}>
                <Icono nombre="escudo" tamano={14} /> El reporte de novedad de llegada se habilita únicamente cuando la
                reserva se encuentra en estado CONFIRMADA.
              </p>
            )}
          </section>

          <section className="form-section" aria-labelledby="titulo-vehiculos">
            <div className="card-head">
              <div>
                <span className="eyebrow">PARQUEADERO (REGLA RP-02)</span>
                <h3 id="titulo-vehiculos">
                  Vehículos registrados · {reserva.vehiculos.length} / {MAX_VEHICULOS}
                </h3>
              </div>
              <Etiqueta tono={reserva.vehiculos.length < MAX_VEHICULOS ? 'available' : 'neutral'}>
                Cupos restantes: {Math.max(MAX_VEHICULOS - reserva.vehiculos.length, 0)}
              </Etiqueta>
            </div>

            <p style={{ fontSize: '11px', color: 'var(--muted)', marginBottom: '10px', lineHeight: 1.5 }}>
              El primer vehículo está incluido sin costo. El segundo vehículo depende de cupos del parqueadero y genera
              un cargo adicional de <strong>$ 20.000 COP</strong> por noche calculado automáticamente en el folio.
            </p>

            {reserva.vehiculos.length > 0 ? (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '12px' }}>
                {reserva.vehiculos.map((placa, indice) => (
                  <Etiqueta key={placa} tono={indice === 0 ? 'available' : 'warning'}>
                    <i /> {placa} {indice === 0 ? '(Incluido)' : '(Adicional)'}
                  </Etiqueta>
                ))}
              </div>
            ) : (
              <p className="hint" style={{ marginTop: 0, marginBottom: '12px' }}>
                No hay vehículos registrados todavía en esta reserva.
              </p>
            )}

            {puedeRegistrarVehiculo ? (
              <form
                onSubmit={formVehiculo.handleSubmit(alRegistrarVehiculo)}
                noValidate
                style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', alignItems: 'flex-end' }}
              >
                <label
                  htmlFor="placa-vehiculo"
                  style={{ display: 'grid', gap: '6px', fontSize: '9px', fontWeight: 800, color: '#53615a' }}
                >
                  PLACA DEL VEHÍCULO
                  <input
                    id="placa-vehiculo"
                    placeholder="Ej. ABC123"
                    maxLength={9}
                    aria-invalid={formVehiculo.formState.errors.placa ? true : undefined}
                    style={{
                      height: '40px',
                      padding: '0 11px',
                      border: '1px solid #d5dcd8',
                      borderRadius: '8px',
                      fontSize: '11px',
                      textTransform: 'uppercase',
                    }}
                    {...formVehiculo.register('placa')}
                  />
                </label>
                <button
                  type="submit"
                  className="primary compact"
                  disabled={formVehiculo.formState.isSubmitting}
                >
                  <Icono nombre="mas" tamano={14} />{' '}
                  {formVehiculo.formState.isSubmitting
                    ? 'Registrando…'
                    : reserva.vehiculos.length === 0
                      ? 'Registrar 1er vehículo'
                      : 'Registrar 2do vehículo (adicional)'}
                </button>
                {formVehiculo.formState.errors.placa && (
                  <p className="error" role="alert" style={{ flexBasis: '100%' }}>
                    {formVehiculo.formState.errors.placa.message}
                  </p>
                )}
              </form>
            ) : (
              <p className="hint" style={{ marginTop: 0 }}>
                <Icono nombre="escudo" tamano={14} />{' '}
                {reserva.vehiculos.length >= MAX_VEHICULOS
                  ? 'Ya alcanzó el máximo de 2 vehículos permitidos por reserva.'
                  : 'No es posible registrar vehículos en una reserva terminada o con folio cerrado.'}
              </p>
            )}
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
                      Documento {o.documento} · nacido el {fechaCorta(o.fechaNacimiento)}{' '}
                      {o.fechaNacimiento.slice(0, 4)}
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
              <span className="eyebrow">FOLIO DE CONSULTA</span>
              <h3 id="titulo-folio">{folio.cerrado ? 'Folio cerrado' : 'Folio abierto'}</h3>
            </div>
            <Etiqueta tono={folio.cerrado ? 'neutral' : 'available'}>
              {folio.cerrado ? 'CERRADO' : 'ABIERTO'}
            </Etiqueta>
          </div>

          <MovimientosFolio folio={folio} />

          {folio.autorizacionCierre && (
            <p className="policy-note">
              <Icono nombre="escudo" tamano={14} /> Cierre con saldo autorizado por {folio.autorizacionCierre.autor}:{' '}
              {folio.autorizacionCierre.motivo}
            </p>
          )}

          <p className="hint">
            <Icono nombre="billetera" tamano={14} /> Los pagos de anticipo y liquidación son registrados únicamente por
            recepción.
          </p>

          <div className="acciones-folio">
            {puedeCancelar && (
              <button type="button" className="danger-link" onClick={() => setMostrarCancelar(true)}>
                Cancelar según la política congelada
              </button>
            )}
            {!activa && (
              <p className="hint">
                La reserva está {reserva.estado.toLowerCase().replace('_', ' ')}: no admite nuevas modificaciones.
              </p>
            )}
          </div>

          <p className="policy-note">
            <Icono nombre="escudo" tamano={14} /> Política de cancelación versión {reserva.versionPolitica}, congelada
            al crear la reserva.
          </p>
        </aside>
      </div>

      {mostrarCancelar && (
        <DialogoCancelarReserva
          codigo={reserva.codigo}
          estado={reserva.estado}
          versionPolitica={reserva.versionPolitica}
          ocupado={procesandoCancelacion}
          onConfirmar={ejecutarCancelacion}
          onCancelar={() => setMostrarCancelar(false)}
        />
      )}
    </section>
  );
}
