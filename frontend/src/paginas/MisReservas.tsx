import { zodResolver } from '@hookform/resolvers/zod';
import axios from 'axios';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { Aviso } from '../componentes/Aviso';
import { Cargando } from '../componentes/Cargando';
import { DialogoCancelarReserva } from '../componentes/DialogoCancelarReserva';
import { DialogoConfirmacion } from '../componentes/DialogoConfirmacion';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { EtiquetaEstado } from '../componentes/EtiquetaEstado';
import { Icono } from '../componentes/Icono';
import { MovimientosFolio } from '../componentes/MovimientosFolio';
import { fotoDeApartamento, nombreDeApartamento } from '../datos/fotos';
import {
  esquemaBuscarReservaCodigo,
  type FormularioBuscarReservaCodigo,
} from '../esquemas/reserva';
import type { UsuarioResponse } from '../modelos/auth';
import type { ErrorResponse } from '../modelos/error';
import type { Folio } from '../modelos/folio';
import type { ReservaDetalle } from '../modelos/reserva';
import { consultarFolio } from '../servicios/folioServicio';
import { cancelarReserva, consultarReserva } from '../servicios/reservaServicio';
import { borrarSesion } from '../servicios/sesion';
import { consultarMiPerfil } from '../servicios/usuarioServicio';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';
import { fechaCorta, hora, pesos } from '../utilidades/formato';
import {
  leerCodigosReserva,
  olvidarCodigoReserva,
  recordarCodigoReserva,
} from '../utilidades/misReservasHuesped';

interface ReservaFallida {
  codigo: string;
  mensaje: string;
  estadoHttp?: number;
}

function formatoErrorBackend(error: unknown): string {
  const base = mensajeDeError(error);
  if (axios.isAxiosError<ErrorResponse>(error) && error.response?.data?.detalles?.length) {
    return `${base} (${error.response.data.detalles.join(' · ')})`;
  }
  return base;
}

function mensajeConsultaReserva(error: unknown, codigo: string): string {
  const status = estadoHttp(error);
  if (status === 401) return 'Su sesión expiró o no es válida. Inicie sesión nuevamente.';
  if (status === 403) return `No tiene acceso a la reserva ${codigo} o no pertenece a su cuenta.`;
  if (status === 404) return `No existe ninguna reserva con el código ${codigo}.`;
  return formatoErrorBackend(error);
}

export default function MisReservas() {
  const [recargaLista, setRecargaLista] = useState(0);
  const [estadoInicial, setEstadoInicial] = useState<{
    tick: number;
    perfil: UsuarioResponse | null;
    reservas: ReservaDetalle[];
    fallidas: ReservaFallida[];
    errorGlobal: string | null;
    sesionExpirada: boolean;
  } | null>(null);

  const [seleccionada, setSeleccionada] = useState<string | null>(null);
  const [recargaFolio, setRecargaFolio] = useState(0);
  const [aviso, setAviso] = useState<string | null>(null);
  const cerrarAviso = useCallback(() => setAviso(null), []);

  const [codigoAQuitar, setCodigoAQuitar] = useState<string | null>(null);
  const [cancelandoReserva, setCancelandoReserva] = useState<ReservaDetalle | null>(null);
  const [procesandoCancelacion, setProcesandoCancelacion] = useState(false);
  const [errorAccion, setErrorAccion] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioBuscarReservaCodigo>({
    resolver: zodResolver(esquemaBuscarReservaCodigo),
    mode: 'onTouched',
    defaultValues: { codigo: '' },
  });

  useEffect(() => {
    const controlador = new AbortController();
    let vigente = true;

    async function cargar() {
      try {
        const perfil = await consultarMiPerfil(controlador.signal);
        if (!vigente) return;

        if (perfil.rol !== 'HUESPED') {
          setEstadoInicial({
            tick: recargaLista,
            perfil,
            reservas: [],
            fallidas: [],
            errorGlobal: null,
            sesionExpirada: false,
          });
          return;
        }

        const codigos = leerCodigosReserva(perfil.documento);
        const resultados = await Promise.allSettled(codigos.map((codigo) => consultarReserva(codigo)));
        if (!vigente) return;

        const reservas: ReservaDetalle[] = [];
        const fallidas: ReservaFallida[] = [];

        resultados.forEach((resultado, indice) => {
          const codigo = codigos[indice];
          if (resultado.status === 'fulfilled') {
            const reserva = resultado.value;
            if (reserva.titular.documento === perfil.documento) {
              reservas.push(reserva);
            } else {
              fallidas.push({
                codigo,
                mensaje: `No tiene acceso a la reserva ${codigo} o no pertenece a su cuenta.`,
                estadoHttp: 403,
              });
            }
          } else {
            fallidas.push({
              codigo,
              mensaje: mensajeConsultaReserva(resultado.reason, codigo),
              estadoHttp: estadoHttp(resultado.reason),
            });
          }
        });

        setEstadoInicial({
          tick: recargaLista,
          perfil,
          reservas,
          fallidas,
          errorGlobal: null,
          sesionExpirada: false,
        });
      } catch (error) {
        if (axios.isCancel(error) || !vigente) return;
        const expirada = estadoHttp(error) === 401;
        if (expirada) borrarSesion();
        setEstadoInicial({
          tick: recargaLista,
          perfil: null,
          reservas: [],
          fallidas: [],
          errorGlobal: expirada
            ? 'Su sesión expiró o no es válida. Inicie sesión nuevamente para consultar sus reservas.'
            : formatoErrorBackend(error),
          sesionExpirada: expirada,
        });
      }
    }

    cargar();
    return () => {
      vigente = false;
      controlador.abort();
    };
  }, [recargaLista]);

  const vigente = estadoInicial?.tick === recargaLista ? estadoInicial : null;
  const cargando = vigente === null;
  const perfil = vigente?.perfil ?? null;
  const reservas = vigente?.reservas ?? [];
  const fallidas = vigente?.fallidas ?? [];
  const codigoActivo =
    seleccionada && reservas.some((r) => r.codigo === seleccionada)
      ? seleccionada
      : (reservas[0]?.codigo ?? null);
  const reservaActiva = reservas.find((r) => r.codigo === codigoActivo) ?? null;

  async function buscarPorCodigo({ codigo }: FormularioBuscarReservaCodigo) {
    if (!perfil || perfil.rol !== 'HUESPED') return;
    setErrorAccion(null);

    try {
      const reserva = await consultarReserva(codigo);
      if (reserva.titular.documento !== perfil.documento) {
        setError('codigo', {
          message: `La reserva ${codigo} no corresponde al titular autenticado.`,
        });
        return;
      }

      recordarCodigoReserva(perfil.documento, reserva.codigo);
      setSeleccionada(reserva.codigo);
      setRecargaLista((n) => n + 1);
      setRecargaFolio((n) => n + 1);
      reset({ codigo: '' });
      setAviso(`Reserva ${reserva.codigo} agregada a su lista local.`);
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        setRecargaLista((n) => n + 1);
        return;
      }
      setError('codigo', { message: mensajeConsultaReserva(error, codigo) });
    }
  }

  function confirmarQuitarCodigo() {
    if (!perfil || !codigoAQuitar) return;
    olvidarCodigoReserva(perfil.documento, codigoAQuitar);
    setAviso(`Código ${codigoAQuitar} quitado de este navegador.`);
    setCodigoAQuitar(null);
    setRecargaLista((n) => n + 1);
  }

  async function ejecutarCancelacion(motivo: string) {
    if (!cancelandoReserva) return;
    setProcesandoCancelacion(true);
    setErrorAccion(null);
    try {
      const actualizada = await cancelarReserva(cancelandoReserva.codigo, motivo);
      setAviso(`Reserva ${actualizada.codigo} cancelada según política v${actualizada.versionPolitica}.`);
      setSeleccionada(actualizada.codigo);
      setRecargaLista((n) => n + 1);
      setRecargaFolio((n) => n + 1);
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        setRecargaLista((n) => n + 1);
      } else {
        setErrorAccion(formatoErrorBackend(error));
      }
    } finally {
      setProcesandoCancelacion(false);
      setCancelandoReserva(null);
    }
  }

  if (vigente?.sesionExpirada) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="MI CUENTA"
          titulo="Mis reservas"
          descripcion="Inicie sesión nuevamente para consultar sus reservas y folios."
        />
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {vigente.errorGlobal}
        </p>
        <Link className="primary compact" to="/login" state={{ desde: '/mis-reservas' }}>
          Ir a ingresar
        </Link>
      </section>
    );
  }

  if (perfil && perfil.rol !== 'HUESPED') {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="MI CUENTA"
          titulo="Mis reservas"
          descripcion="Esta sección está destinada al huésped titular."
        />
        <div className="empty">
          <Icono nombre="escudo" tamano={28} />
          <h3>Vista exclusiva para huéspedes</h3>
          <p style={{ marginTop: '6px' }}>
            Para consultar todas las reservas del alojamiento utilice el módulo de Recepción.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section>
      <EncabezadoPagina
        antetitulo="MI CUENTA"
        titulo="Mis reservas"
        descripcion="Consulte las reservas de las que es titular, su folio y las acciones permitidas."
      >
        <Link className="primary" to="/apartamentos">
          <Icono nombre="buscar" tamano={16} /> Buscar apartamentos
        </Link>
      </EncabezadoPagina>

      <p className="policy-note" style={{ marginBottom: '14px', marginTop: 0 }}>
        <Icono nombre="escudo" tamano={14} /> Esta vista muestra las reservas recordadas en este navegador y las
        incorporadas manualmente por código. No constituye un listado histórico completo de la base de datos.
      </p>

      <form
        className="filtros"
        style={{ gridTemplateColumns: 'minmax(0, 1fr) auto' }}
        onSubmit={handleSubmit(buscarPorCodigo)}
        noValidate
        aria-label="Consultar reserva por código"
      >
        <label htmlFor="codigo-busqueda">
          CONSULTAR RESERVA POR CÓDIGO (FORMATO RES-AAAA-NNNNN)
          <input
            id="codigo-busqueda"
            placeholder="Ej. RES-2026-00001"
            maxLength={14}
            aria-invalid={errors.codigo ? true : undefined}
            {...register('codigo')}
          />
          {errors.codigo && (
            <span className="error" role="alert">
              {errors.codigo.message}
            </span>
          )}
        </label>
        <div className="button-row">
          <button type="submit" className="primary compact" disabled={isSubmitting || cargando}>
            <Icono nombre="buscar" tamano={14} /> {isSubmitting ? 'Consultando…' : 'Consultar'}
          </button>
        </div>
      </form>

      {vigente?.errorGlobal && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {vigente.errorGlobal}
        </p>
      )}

      {errorAccion && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {errorAccion}
        </p>
      )}

      {cargando && <Cargando filas={4} texto="Cargando sus reservas recordadas…" />}

      {!cargando && !vigente?.errorGlobal && (
        <div className="reservation-layout">
          <div>
            <div className="reservation-list" aria-label="Listado de mis reservas">
              {reservas.length === 0 && fallidas.length === 0 && (
                <div className="empty">
                  <Icono nombre="calendario" tamano={28} />
                  <h3>No hay reservas recordadas en este navegador</h3>
                  <p style={{ marginTop: '6px' }}>
                    Si creó una reserva desde otro navegador o dispositivo, ingrese su código arriba (por ejemplo{' '}
                    <code>RES-2026-00001</code>) para consultarla.
                  </p>
                </div>
              )}

              {reservas.map((r) => (
                <button
                  type="button"
                  key={r.codigo}
                  className={`reservation-row ${codigoActivo === r.codigo ? 'selected' : ''}`}
                  onClick={() => setSeleccionada(r.codigo)}
                  aria-pressed={codigoActivo === r.codigo}
                >
                  <img className="res-foto" src={fotoDeApartamento(r.identificacionApartamento)} alt="" />
                  <span className="res-main">
                    <span>{r.codigo}</span>
                    <strong>{nombreDeApartamento(r.identificacionApartamento)}</strong>
                    <small>
                      {fechaCorta(r.fechaEntrada)} → {fechaCorta(r.fechaSalida)} · {r.canalOrigen}
                    </small>
                  </span>
                  <span className="res-value">
                    <EtiquetaEstado estado={r.estado} />
                    <strong>{pesos(r.cotizacionCongelada.valorTotal)}</strong>
                  </span>
                  <Icono nombre="flecha" tamano={18} />
                </button>
              ))}

              {fallidas.map((item) => (
                <div
                  key={item.codigo}
                  className="form-section"
                  style={{ padding: '12px 14px', display: 'grid', gap: '8px' }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '8px' }}>
                    <strong style={{ fontSize: '12px' }}>{item.codigo}</strong>
                    <div className="button-row">
                      <button
                        type="button"
                        className="secondary compact"
                        onClick={() => setRecargaLista((n) => n + 1)}
                      >
                        Reintentar
                      </button>
                      <button
                        type="button"
                        className="secondary compact peligro"
                        onClick={() => setCodigoAQuitar(item.codigo)}
                      >
                        Quitar
                      </button>
                    </div>
                  </div>
                  <p className="form-error" role="alert" style={{ margin: 0 }}>
                    <Icono nombre="alerta" tamano={14} /> {item.mensaje}
                  </p>
                </div>
              ))}
            </div>
          </div>

          {reservaActiva && (
            <PanelFolioHuesped
              reserva={reservaActiva}
              recargaFolio={recargaFolio}
              onSolicitarCancelar={() => setCancelandoReserva(reservaActiva)}
            />
          )}
        </div>
      )}

      {codigoAQuitar && (
        <DialogoConfirmacion
          titulo={`Quitar ${codigoAQuitar} de este navegador`}
          textoConfirmar="Quitar código"
          peligroso
          onConfirmar={confirmarQuitarCodigo}
          onCancelar={() => setCodigoAQuitar(null)}
        >
          <p>
            Se eliminará <strong>{codigoAQuitar}</strong> de la lista recordada en este navegador. La reserva no se
            modifica ni se borra del servidor y podrá consultarla de nuevo ingresando su código.
          </p>
        </DialogoConfirmacion>
      )}

      {cancelandoReserva && (
        <DialogoCancelarReserva
          codigo={cancelandoReserva.codigo}
          estado={cancelandoReserva.estado}
          versionPolitica={cancelandoReserva.versionPolitica}
          ocupado={procesandoCancelacion}
          onConfirmar={ejecutarCancelacion}
          onCancelar={() => setCancelandoReserva(null)}
        />
      )}

      {aviso && <Aviso mensaje={aviso} onCerrar={cerrarAviso} />}
    </section>
  );
}

interface PanelFolioHuespedProps {
  reserva: ReservaDetalle;
  recargaFolio: number;
  onSolicitarCancelar: () => void;
}

function PanelFolioHuesped({ reserva, recargaFolio, onSolicitarCancelar }: PanelFolioHuespedProps) {
  const clave = `${reserva.codigo}|${recargaFolio}`;
  const [estadoFolio, setEstadoFolio] = useState<{
    clave: string;
    folio: Folio | null;
    error: string | null;
  } | null>(null);

  useEffect(() => {
    let vigente = true;
    consultarFolio(reserva.codigo)
      .then((folio) => {
        if (vigente) setEstadoFolio({ clave, folio, error: null });
      })
      .catch((error) => {
        if (vigente) setEstadoFolio({ clave, folio: null, error: formatoErrorBackend(error) });
      });

    return () => {
      vigente = false;
    };
  }, [reserva.codigo, clave]);

  const vigente = estadoFolio?.clave === clave ? estadoFolio : null;
  const folio = vigente?.folio ?? null;
  const error = vigente?.error ?? null;
  const puedeCancelar =
    (reserva.estado === 'PENDIENTE' || reserva.estado === 'CONFIRMADA') && Boolean(folio && !folio.cerrado);

  return (
    <aside className="folio-card" aria-label={`Folio de ${reserva.codigo}`} aria-live="polite">
      <div className="folio-head">
        <div>
          <span className="eyebrow">FOLIO DE RESERVA</span>
          <h3>{reserva.codigo}</h3>
        </div>
        <EtiquetaEstado estado={reserva.estado} />
      </div>

      <div className="folio-summary">
        <div>
          <small>Estancia</small>
          <b>
            {fechaCorta(reserva.fechaEntrada)} → {fechaCorta(reserva.fechaSalida)}
          </b>
        </div>
        <div>
          <small>Apartamento</small>
          <b>{nombreDeApartamento(reserva.identificacionApartamento)}</b>
        </div>
        <div>
          <small>Titular</small>
          <b>{reserva.titular.nombreCompleto}</b>
        </div>
        <div>
          <small>Llegada estimada</small>
          <b>{hora(reserva.horaEstimadaLlegada)}</b>
        </div>
        <div>
          <small>Valor de estancia</small>
          <b>{pesos(reserva.cotizacionCongelada.valorTotal)}</b>
        </div>
        <div>
          <small>Vehículos</small>
          <b>{reserva.vehiculos.length} / 2 registrados</b>
        </div>
      </div>

      {!vigente && <Cargando filas={3} texto="Consultando folio…" />}

      {error && (
        <p className="form-error" role="alert">
          <Icono nombre="alerta" tamano={14} /> {error}
        </p>
      )}

      {folio && (
        <>
          <MovimientosFolio folio={folio} />

          <p className="hint">
            <Icono nombre="billetera" tamano={14} /> Consulta de folio únicamente informativa. Los pagos y el cierre los
            registra recepción.
          </p>

          <div className="acciones-folio">
            <Link className="primary full" to={`/mis-reservas/${reserva.codigo}`}>
              Gestionar reserva y vehículos <Icono nombre="flecha" tamano={18} />
            </Link>

            {puedeCancelar && (
              <button type="button" className="danger-link" onClick={onSolicitarCancelar}>
                Cancelar según política congelada
              </button>
            )}
          </div>
        </>
      )}

      <p className="policy-note">
        <Icono nombre="escudo" tamano={14} /> Política de cancelación versión {reserva.versionPolitica}, congelada al
        crear la reserva.
      </p>
    </aside>
  );
}
