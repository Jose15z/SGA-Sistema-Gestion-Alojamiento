import { zodResolver } from '@hookform/resolvers/zod';
import axios from 'axios';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';
import { Link, useLocation, useSearchParams } from 'react-router';
import { Aviso } from '../componentes/Aviso';
import { Cargando } from '../componentes/Cargando';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta } from '../componentes/Etiqueta';
import { EtiquetaEstado } from '../componentes/EtiquetaEstado';
import { Icono } from '../componentes/Icono';
import { fotoDeApartamento } from '../datos/fotos';
import { crearEsquemaReservaHuesped, type FormularioReservaHuesped } from '../esquemas/reserva';
import { nombreTemporada, type Apartamento } from '../modelos/apartamento';
import type { UsuarioResponse } from '../modelos/auth';
import type { ErrorResponse } from '../modelos/error';
import type { Cotizacion, ReservaDetalle } from '../modelos/reserva';
import { consultarApartamento, cotizarEstancia } from '../servicios/apartamentoServicio';
import { crearReserva } from '../servicios/reservaServicio';
import { borrarSesion, leerSesion } from '../servicios/sesion';
import { consultarMiPerfil } from '../servicios/usuarioServicio';
import { erroresDeCampo, estadoHttp, mensajeDeError } from '../utilidades/errores';
import { hoyISO } from '../utilidades/fechas';
import { fechaCorta, hora, noches, pesos, sumarDias } from '../utilidades/formato';
import { recordarCodigoReserva } from '../utilidades/misReservasHuesped';

interface SeleccionReservaState {
  identificacionApartamento?: unknown;
  fechaEntrada?: unknown;
  fechaSalida?: unknown;
  totalOcupantes?: unknown;
  fechasNacimiento?: unknown;
}

interface SeleccionValidada {
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  totalOcupantes: number;
  fechasNacimiento: string[];
}

function esFechaIso(valor: unknown): valor is string {
  return typeof valor === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(valor);
}

function extraerSeleccionInicial(
  state: unknown,
  searchParams: URLSearchParams,
): SeleccionValidada | null {
  const hoy = hoyISO();
  const origenState = (state && typeof state === 'object' ? state : {}) as SeleccionReservaState;

  const idRaw =
    typeof origenState.identificacionApartamento === 'string'
      ? origenState.identificacionApartamento
      : (searchParams.get('apartamento') ?? '');
  const identificacionApartamento = /^APT-[0-9]{3}$/.test(idRaw.trim()) ? idRaw.trim() : '';

  const entradaRaw =
    typeof origenState.fechaEntrada === 'string'
      ? origenState.fechaEntrada
      : (searchParams.get('fechaEntrada') ?? '');
  const salidaRaw =
    typeof origenState.fechaSalida === 'string'
      ? origenState.fechaSalida
      : (searchParams.get('fechaSalida') ?? '');

  if (!identificacionApartamento || !esFechaIso(entradaRaw) || !esFechaIso(salidaRaw)) {
    return null;
  }
  if (entradaRaw < hoy || salidaRaw <= entradaRaw) {
    return null;
  }

  const totalRaw =
    typeof origenState.totalOcupantes === 'number'
      ? origenState.totalOcupantes
      : Number(searchParams.get('totalOcupantes'));

  const fechasOrigen = Array.isArray(origenState.fechasNacimiento)
    ? origenState.fechasNacimiento
    : searchParams.getAll('fechasNacimiento');
  const fechasNacimiento = fechasOrigen.filter((f): f is string => esFechaIso(f) && f < hoy);

  const totalOcupantes =
    Number.isInteger(totalRaw) && totalRaw >= 1 && totalRaw <= 50
      ? totalRaw
      : fechasNacimiento.length >= 1
        ? fechasNacimiento.length
        : 1;

  return {
    identificacionApartamento,
    fechaEntrada: entradaRaw,
    fechaSalida: salidaRaw,
    totalOcupantes,
    fechasNacimiento,
  };
}

function prepararFechasAcompanantes(
  totalOcupantes: number,
  fechasNacimiento: string[],
  fechaNacimientoTitular: string,
): string[] {
  const cantidadAcompanantes = Math.max(totalOcupantes - 1, 0);
  if (cantidadAcompanantes === 0) return [];

  const restantes = [...fechasNacimiento];
  if (restantes.length > 0) {
    const indiceTitular = restantes.indexOf(fechaNacimientoTitular);
    restantes.splice(indiceTitular >= 0 ? indiceTitular : 0, 1);
  }

  return Array.from({ length: cantidadAcompanantes }, (_, i) => restantes[i] ?? '');
}

function formatoErrorBackend(error: unknown): string {
  const base = mensajeDeError(error);
  if (axios.isAxiosError<ErrorResponse>(error) && error.response?.data?.detalles?.length) {
    return `${base} (${error.response.data.detalles.join(' · ')})`;
  }
  return base;
}

export default function NuevaReserva() {
  const ubicacion = useLocation();
  const [searchParams] = useSearchParams();
  const [seleccion] = useState(() => extraerSeleccionInicial(ubicacion.state, searchParams));

  const claveCarga = seleccion ? `${seleccion.identificacionApartamento}|${ubicacion.key}` : 'sin-seleccion';
  const [estadoCarga, setEstadoCarga] = useState<{
    clave: string;
    perfil: UsuarioResponse | null;
    apartamento: Apartamento | null;
    error: string | null;
    sesionExpirada: boolean;
  } | null>(null);

  const [reservaCreada, setReservaCreada] = useState<ReservaDetalle | null>(null);

  useEffect(() => {
    if (!seleccion) return;
    const controlador = new AbortController();
    let vigente = true;

    Promise.all([
      consultarMiPerfil(controlador.signal),
      consultarApartamento(seleccion.identificacionApartamento, controlador.signal),
    ])
      .then(([perfil, apartamento]) => {
        if (!vigente) return;
        setEstadoCarga({
          clave: claveCarga,
          perfil,
          apartamento,
          error: null,
          sesionExpirada: false,
        });
      })
      .catch((error) => {
        if (axios.isCancel(error) || !vigente) return;
        const expirada = estadoHttp(error) === 401;
        if (expirada) {
          borrarSesion();
        }
        setEstadoCarga({
          clave: claveCarga,
          perfil: null,
          apartamento: null,
          error: expirada
            ? 'Su sesión ha expirado o no es válida. Inicie sesión nuevamente para continuar.'
            : formatoErrorBackend(error),
          sesionExpirada: expirada,
        });
      });

    return () => {
      vigente = false;
      controlador.abort();
    };
  }, [seleccion, claveCarga]);

  if (!seleccion) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="RESERVA EN LÍNEA · CANAL PORTAL"
          titulo="Reservar apartamento"
          descripcion="Seleccione primero un apartamento, fechas y cotización desde el catálogo."
        />
        <div className="empty">
          <Icono nombre="calendario" tamano={28} />
          <h3>Falta seleccionar un apartamento y sus fechas</h3>
          <p style={{ marginTop: '6px' }}>
            Para crear una reserva en línea debe elegir un apartamento disponible, confirmar las fechas y cotizar su
            estancia.
          </p>
          <div className="button-row" style={{ justifyContent: 'center', marginTop: '14px' }}>
            <Link className="primary compact" to="/apartamentos">
              Buscar apartamentos
            </Link>
          </div>
        </div>
      </section>
    );
  }

  const cargaVigente = estadoCarga?.clave === claveCarga ? estadoCarga : null;

  if (!cargaVigente) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="RESERVA EN LÍNEA · CANAL PORTAL"
          titulo="Preparando tu reserva"
          descripcion="Verificando perfil autenticado y disponibilidad de la unidad…"
        >
          <Link className="secondary" to={`/apartamentos/${seleccion.identificacionApartamento}`}>
            Volver al detalle
          </Link>
        </EncabezadoPagina>
        <Cargando filas={5} texto="Cargando datos del titular y del apartamento…" />
      </section>
    );
  }

  if (cargaVigente.sesionExpirada) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="SESIÓN REQUERIDA"
          titulo="Inicie sesión nuevamente"
          descripcion="Su sesión expiró antes de confirmar la reserva."
        />
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {cargaVigente.error}
        </p>
        <div className="empty">
          <Icono nombre="escudo" tamano={28} />
          <h3>Sesión expirada</h3>
          <p style={{ marginTop: '6px' }}>
            Ingrese nuevamente con su cuenta de huésped para continuar reservando a su nombre.
          </p>
          <div className="button-row" style={{ justifyContent: 'center', marginTop: '14px' }}>
            <Link
              className="primary compact"
              to="/login"
              state={{ desde: `${ubicacion.pathname}${ubicacion.search}` }}
            >
              Ir a ingresar
            </Link>
          </div>
        </div>
      </section>
    );
  }

  if (cargaVigente.error || !cargaVigente.perfil || !cargaVigente.apartamento) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="RESERVA EN LÍNEA · CANAL PORTAL"
          titulo="No fue posible iniciar la reserva"
          descripcion="Verifique el apartamento seleccionado o su conexión con el servidor."
        >
          <Link className="secondary" to="/apartamentos">
            Volver al catálogo
          </Link>
        </EncabezadoPagina>
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {cargaVigente.error ?? 'Error al cargar los datos iniciales.'}
        </p>
      </section>
    );
  }

  const { perfil, apartamento } = cargaVigente;

  if (perfil.rol !== 'HUESPED' || leerSesion()?.rol !== 'HUESPED') {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="CANAL PORTAL"
          titulo="Acceso exclusivo para huéspedes"
          descripcion="Las reservas del portal solo pueden crearse con una cuenta de huésped a nombre propio."
        />
        <div className="empty">
          <Icono nombre="escudo" tamano={28} />
          <h3>Perfil no autorizado para reserva por portal</h3>
          <p style={{ marginTop: '6px' }}>
            Si atiende recepción o administración, utilice la opción «Nueva reserva directa».
          </p>
          <div className="button-row" style={{ justifyContent: 'center', marginTop: '14px' }}>
            <Link className="secondary compact" to="/apartamentos">
              Volver a disponibilidad
            </Link>
          </div>
        </div>
      </section>
    );
  }

  if (!apartamento.activo) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo={`${apartamento.identificacion} · NO VENDIBLE`}
          titulo={apartamento.nombre}
          descripcion="Este apartamento no está disponible para la venta."
        />
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> El apartamento {apartamento.identificacion} está inactivo y no admite
          nuevas reservas.
        </p>
        <Link className="primary compact" to="/apartamentos">
          Buscar otro apartamento
        </Link>
      </section>
    );
  }

  if (seleccion.totalOcupantes > apartamento.capacidad) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo={`${apartamento.identificacion} · CAPACIDAD`}
          titulo={apartamento.nombre}
          descripcion="La composición recibida supera la capacidad máxima permitida."
        />
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> Se solicitaron {seleccion.totalOcupantes} ocupantes, pero{' '}
          {apartamento.identificacion} admite máximo {apartamento.capacidad} personas.
        </p>
        <div className="button-row" style={{ justifyContent: 'flex-start' }}>
          <Link className="primary compact" to={`/apartamentos/${apartamento.identificacion}`}>
            Ajustar en detalle del apartamento
          </Link>
          <Link className="secondary compact" to="/apartamentos">
            Volver al catálogo
          </Link>
        </div>
      </section>
    );
  }

  if (reservaCreada) {
    return <ConfirmacionReservaCreada reserva={reservaCreada} apartamento={apartamento} />;
  }

  return (
    <FormularioReservaHuesped
      perfil={perfil}
      apartamento={apartamento}
      seleccion={seleccion}
      onReservaCreada={setReservaCreada}
    />
  );
}

interface FormularioReservaHuespedProps {
  perfil: UsuarioResponse;
  apartamento: Apartamento;
  seleccion: SeleccionValidada;
  onReservaCreada: (reserva: ReservaDetalle) => void;
}

function FormularioReservaHuesped({
  perfil,
  apartamento,
  seleccion,
  onReservaCreada,
}: FormularioReservaHuespedProps) {
  const esquema = useMemo(
    () => crearEsquemaReservaHuesped(perfil.documento, apartamento.capacidad),
    [perfil.documento, apartamento.capacidad],
  );

  const [valoresIniciales] = useState<FormularioReservaHuesped>(() => {
    const fechasAcompanantes = prepararFechasAcompanantes(
      seleccion.totalOcupantes,
      seleccion.fechasNacimiento,
      perfil.fechaNacimiento,
    );
    return {
      fechaEntrada: seleccion.fechaEntrada,
      fechaSalida: seleccion.fechaSalida,
      horaEstimadaLlegada: '15:00',
      mascotas: 0,
      acompanantes: fechasAcompanantes.map((fechaNacimiento) => ({
        documento: '',
        nombreCompleto: '',
        fechaNacimiento,
      })),
    };
  });

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioReservaHuesped>({
    resolver: zodResolver(esquema),
    mode: 'onTouched',
    defaultValues: valoresIniciales,
  });

  const { fields, append, remove } = useFieldArray({ control, name: 'acompanantes' });

  const [entrada, salida, horaLlegada, mascotasObservadas, acompanantes] = useWatch({
    control,
    name: ['fechaEntrada', 'fechaSalida', 'horaEstimadaLlegada', 'mascotas', 'acompanantes'],
  });

  const [recotizarTick, setRecotizarTick] = useState(0);
  const [sesionVencidaAlEnviar, setSesionVencidaAlEnviar] = useState(false);

  const hoy = hoyISO();
  const ayer = sumarDias(hoy, -1);
  const totalOcupantes = 1 + acompanantes.length;
  const capacidadCompleta = totalOcupantes >= apartamento.capacidad;
  const fechasAcompanantesValidas = acompanantes.every(
    (a) => /^\d{4}-\d{2}-\d{2}$/.test(a.fechaNacimiento) && a.fechaNacimiento < hoy,
  );
  const estanciaValida =
    /^\d{4}-\d{2}-\d{2}$/.test(entrada) &&
    /^\d{4}-\d{2}-\d{2}$/.test(salida) &&
    entrada >= hoy &&
    salida > entrada;

  const solicitudCotizacion =
    estanciaValida &&
    totalOcupantes <= apartamento.capacidad &&
    perfil.fechaNacimiento < hoy &&
    fechasAcompanantesValidas
      ? JSON.stringify({
          identificacionApartamento: apartamento.identificacion,
          fechaEntrada: entrada,
          fechaSalida: salida,
          ocupantes: [
            { fechaNacimiento: perfil.fechaNacimiento },
            ...acompanantes.map((a) => ({ fechaNacimiento: a.fechaNacimiento })),
          ],
        })
      : null;

  const claveCotizacion = solicitudCotizacion ? `${recotizarTick}|${solicitudCotizacion}` : null;

  const [resultadoCotizacion, setResultadoCotizacion] = useState<{
    clave: string;
    cotizacion: Cotizacion | null;
    error: string | null;
  } | null>(null);

  useEffect(() => {
    if (!solicitudCotizacion || !claveCotizacion) return;
    const controlador = new AbortController();
    let vigente = true;

    const temporizador = window.setTimeout(() => {
      cotizarEstancia(JSON.parse(solicitudCotizacion), controlador.signal)
        .then((cotizacion) => {
          if (!vigente) return;
          setResultadoCotizacion({ clave: claveCotizacion, cotizacion, error: null });
        })
        .catch((error) => {
          if (axios.isCancel(error) || !vigente) return;
          setResultadoCotizacion({
            clave: claveCotizacion,
            cotizacion: null,
            error: formatoErrorBackend(error),
          });
        });
    }, 350);

    return () => {
      vigente = false;
      window.clearTimeout(temporizador);
      controlador.abort();
    };
  }, [solicitudCotizacion, claveCotizacion]);

  const vigenteCotizacion =
    claveCotizacion !== null && resultadoCotizacion?.clave === claveCotizacion ? resultadoCotizacion : null;
  const cotizando = claveCotizacion !== null && vigenteCotizacion === null;
  const cotizacionVigente = vigenteCotizacion?.cotizacion ?? null;
  const errorCotizacion = vigenteCotizacion?.error ?? null;

  async function alEnviar(datos: FormularioReservaHuesped) {
    if (!cotizacionVigente || cotizando || errorCotizacion || !apartamento.activo) return;
    setSesionVencidaAlEnviar(false);

    try {
      const reserva = await crearReserva({
        identificacionApartamento: apartamento.identificacion,
        fechaEntrada: datos.fechaEntrada,
        fechaSalida: datos.fechaSalida,
        canalOrigen: 'PORTAL',
        identificadorExterno: null,
        titular: {
          documento: perfil.documento,
          nombreCompleto: perfil.nombreCompleto,
          fechaNacimiento: perfil.fechaNacimiento,
        },
        acompanantes: datos.acompanantes.map((a) => ({
          documento: a.documento.trim(),
          nombreCompleto: a.nombreCompleto.trim(),
          fechaNacimiento: a.fechaNacimiento,
        })),
        horaEstimadaLlegada: datos.horaEstimadaLlegada.trim() || null,
        mascotas: datos.mascotas,
      });

      recordarCodigoReserva(perfil.documento, reserva.codigo);
      onReservaCreada(reserva);
    } catch (error) {
      if (estadoHttp(error) === 401) {
        borrarSesion();
        setSesionVencidaAlEnviar(true);
        setError('root.servidor', {
          message: 'Su sesión ha expirado. Inicie sesión nuevamente para crear la reserva.',
        });
        return;
      }

      const porCampo = erroresDeCampo(error);
      const entradas = Object.entries(porCampo);
      if (entradas.length > 0) {
        let asignados = 0;
        for (const [campo, mensaje] of entradas) {
          if (
            campo === 'fechaEntrada' ||
            campo === 'fechaSalida' ||
            campo === 'horaEstimadaLlegada' ||
            campo === 'mascotas' ||
            /^acompanantes\.\d+\.(documento|nombreCompleto|fechaNacimiento)$/.test(campo)
          ) {
            setError(campo as never, { message: mensaje });
            asignados++;
          }
        }
        if (asignados > 0) return;
      }

      setError('root.servidor', { message: formatoErrorBackend(error) });
    }
  }

  const totalNoches = estanciaValida ? noches(entrada, salida) : 0;
  const facturables = cotizacionVigente?.detalleNoches[0]?.ocupantesFacturables;

  return (
    <section>
      <EncabezadoPagina
        antetitulo="CANAL DE ORIGEN · PORTAL"
        titulo="Confirmar datos de reserva"
        descripcion="La reserva nace en estado PENDIENTE a su nombre y con el valor oficial calculado por Siete Raíces."
      >
        <Link className="secondary" to={`/apartamentos/${apartamento.identificacion}`}>
          <Icono nombre="cerrar" /> Volver al detalle
        </Link>
      </EncabezadoPagina>

      {apartamento.estadoOperativo === 'FUERA_DE_SERVICIO' && (
        <p className="form-error bloque" role="status">
          <Icono nombre="alerta" tamano={16} /> Aviso operativo: la unidad está marcada actualmente como fuera de
          servicio; el backend validará disponibilidad y bloqueos para sus fechas.
        </p>
      )}

      <form className="direct-booking" onSubmit={handleSubmit(alEnviar)} noValidate>
        <div className="booking-form-main">
          <section className="form-section" aria-labelledby="paso-estancia">
            <div className="form-section-title">
              <span>1</span>
              <div>
                <h3 id="paso-estancia">Estancia, llegada y mascotas</h3>
                <p>
                  Apartamento seleccionado: <strong>{apartamento.identificacion} · {apartamento.nombre}</strong> (hasta{' '}
                  {apartamento.capacidad} personas).
                </p>
              </div>
            </div>

            <div className="form-grid">
              <label htmlFor="fechaEntrada">
                FECHA DE ENTRADA
                <input
                  id="fechaEntrada"
                  type="date"
                  min={hoy}
                  aria-invalid={errors.fechaEntrada ? true : undefined}
                  {...register('fechaEntrada')}
                />
                {errors.fechaEntrada && (
                  <span className="error" role="alert">
                    {errors.fechaEntrada.message}
                  </span>
                )}
              </label>

              <label htmlFor="fechaSalida">
                FECHA DE SALIDA
                <input
                  id="fechaSalida"
                  type="date"
                  min={entrada || hoy}
                  aria-invalid={errors.fechaSalida ? true : undefined}
                  {...register('fechaSalida')}
                />
                {errors.fechaSalida && (
                  <span className="error" role="alert">
                    {errors.fechaSalida.message}
                  </span>
                )}
              </label>

              <label htmlFor="horaEstimadaLlegada">
                HORA ESTIMADA DE LLEGADA
                <input
                  id="horaEstimadaLlegada"
                  type="time"
                  aria-invalid={errors.horaEstimadaLlegada ? true : undefined}
                  {...register('horaEstimadaLlegada')}
                />
                <small>Opcional al crear (formato 24h HH:mm); requerida antes de confirmar (RN-09).</small>
                {errors.horaEstimadaLlegada && (
                  <span className="error" role="alert">
                    {errors.horaEstimadaLlegada.message}
                  </span>
                )}
              </label>

              <label htmlFor="mascotas">
                MASCOTAS (REGLA L-16)
                <select
                  id="mascotas"
                  aria-invalid={errors.mascotas ? true : undefined}
                  {...register('mascotas', { valueAsNumber: true })}
                >
                  <option value={0}>Sin mascotas (0)</option>
                  <option value={1}>1 mascota (máximo permitido)</option>
                </select>
                <small>Siete Raíces admite máximo 1 mascota por reserva.</small>
                {errors.mascotas && (
                  <span className="error" role="alert">
                    {errors.mascotas.message}
                  </span>
                )}
              </label>
            </div>
          </section>

          <section className="form-section" aria-labelledby="paso-titular">
            <div className="form-section-title">
              <span>2</span>
              <div>
                <h3 id="paso-titular">Titular de la reserva (cuenta autenticada)</h3>
                <p>
                  Por seguridad, las reservas por canal PORTAL se registran únicamente a nombre del huésped autenticado.
                </p>
              </div>
            </div>

            <fieldset className="occupant-card" style={{ marginTop: '14px' }}>
              <legend>
                Titular · Solo lectura
                <Etiqueta tono="confirmed">PORTAL</Etiqueta>
              </legend>
              <label htmlFor="titular-documento">
                DOCUMENTO
                <input id="titular-documento" value={perfil.documento} readOnly />
              </label>
              <label htmlFor="titular-nombre">
                NOMBRE COMPLETO
                <input id="titular-nombre" value={perfil.nombreCompleto} readOnly />
              </label>
              <label htmlFor="titular-nacimiento">
                FECHA DE NACIMIENTO
                <input id="titular-nacimiento" type="date" value={perfil.fechaNacimiento} readOnly />
              </label>
            </fieldset>
          </section>

          <section className="form-section" aria-labelledby="paso-acompanantes">
            <div className="form-section-title">
              <span>3</span>
              <div>
                <h3 id="paso-acompanantes">
                  Acompañantes ({fields.length}) · Total grupo {totalOcupantes} / {apartamento.capacidad}
                </h3>
                <p>
                  El titular cuenta como 1 ocupante. Todos cuentan para la capacidad; los menores de 10 años no se
                  cobran (RN-06).
                </p>
              </div>
            </div>

            {fields.length === 0 && (
              <p className="hint" style={{ marginTop: '12px' }}>
                <Icono nombre="usuarios" tamano={14} /> Reserva únicamente para el titular. Puede agregar acompañantes si
                viaja en grupo.
              </p>
            )}

            {fields.map((campo, indice) => {
              const errorItem = errors.acompanantes?.[indice];
              return (
                <fieldset className="occupant-card" key={campo.id}>
                  <legend>
                    Acompañante {indice + 1}
                    <button
                      type="button"
                      onClick={() => remove(indice)}
                      aria-label={`Quitar acompañante ${indice + 1}`}
                    >
                      Quitar
                    </button>
                  </legend>

                  <label htmlFor={`acomp-documento-${indice}`}>
                    DOCUMENTO
                    <input
                      id={`acomp-documento-${indice}`}
                      placeholder="Ej. 1094000123"
                      maxLength={30}
                      aria-invalid={errorItem?.documento ? true : undefined}
                      {...register(`acompanantes.${indice}.documento`)}
                    />
                    {errorItem?.documento && (
                      <span className="error" role="alert">
                        {errorItem.documento.message}
                      </span>
                    )}
                  </label>

                  <label htmlFor={`acomp-nombre-${indice}`}>
                    NOMBRE COMPLETO
                    <input
                      id={`acomp-nombre-${indice}`}
                      placeholder="Nombre y apellidos"
                      maxLength={150}
                      aria-invalid={errorItem?.nombreCompleto ? true : undefined}
                      {...register(`acompanantes.${indice}.nombreCompleto`)}
                    />
                    {errorItem?.nombreCompleto && (
                      <span className="error" role="alert">
                        {errorItem.nombreCompleto.message}
                      </span>
                    )}
                  </label>

                  <label htmlFor={`acomp-nacimiento-${indice}`}>
                    FECHA DE NACIMIENTO
                    <input
                      id={`acomp-nacimiento-${indice}`}
                      type="date"
                      max={ayer}
                      aria-invalid={errorItem?.fechaNacimiento ? true : undefined}
                      {...register(`acompanantes.${indice}.fechaNacimiento`)}
                    />
                    {errorItem?.fechaNacimiento && (
                      <span className="error" role="alert">
                        {errorItem.fechaNacimiento.message}
                      </span>
                    )}
                  </label>
                </fieldset>
              );
            })}

            {errors.acompanantes?.root?.message && (
              <p className="form-error" role="alert" style={{ marginTop: '10px' }}>
                <Icono nombre="alerta" tamano={14} /> {errors.acompanantes.root.message}
              </p>
            )}

            <button
              type="button"
              className="add-occupant"
              onClick={() => append({ documento: '', nombreCompleto: '', fechaNacimiento: '' })}
              disabled={capacidadCompleta}
            >
              <Icono nombre="mas" />{' '}
              {capacidadCompleta
                ? `Capacidad completa (${apartamento.capacidad} personas)`
                : 'Agregar acompañante'}
            </button>
          </section>
        </div>

        <aside className="booking-review" aria-label="Resumen oficial de la reserva">
          <span className="eyebrow">RESUMEN Y COTIZACIÓN</span>
          <h3>
            {apartamento.identificacion} · {apartamento.nombre}
          </h3>
          <div className="review-image">
            <img
              src={fotoDeApartamento(apartamento.identificacion)}
              alt={`Apartamento ${apartamento.nombre}`}
            />
            <Etiqueta tono="available">
              <i /> {apartamento.dormitorios} {apartamento.dormitorios === 1 ? 'dormitorio' : 'dormitorios'} · hasta{' '}
              {apartamento.capacidad} personas
            </Etiqueta>
          </div>

          <div className="review-row">
            <span>Estancia</span>
            <b>{estanciaValida ? `${fechaCorta(entrada)} → ${fechaCorta(salida)}` : '—'}</b>
          </div>
          <div className="review-row">
            <span>Duración</span>
            <b>{totalNoches ? `${totalNoches} ${totalNoches === 1 ? 'noche' : 'noches'}` : '—'}</b>
          </div>
          <div className="review-row">
            <span>Ocupantes</span>
            <b>
              {totalOcupantes} {totalOcupantes === 1 ? 'persona' : 'personas'} (1 titular + {fields.length})
            </b>
          </div>
          <div className="review-row">
            <span>Canal</span>
            <b>PORTAL</b>
          </div>
          <div className="review-row">
            <span>Llegada estimada</span>
            <b>{horaLlegada ? hora(horaLlegada) : 'Sin definir'}</b>
          </div>
          <div className="review-row">
            <span>Mascotas</span>
            <b>{mascotasObservadas === 1 ? '1 mascota' : 'Ninguna'}</b>
          </div>

          <div className="review-total" aria-live="polite">
            <span>Valor total oficial</span>
            <strong>{cotizando ? 'Recotizando…' : cotizacionVigente ? pesos(cotizacionVigente.valorTotal) : '—'}</strong>
            <small>
              {cotizacionVigente
                ? `${facturables ?? 0} facturables × ${cotizacionVigente.totalNoches} noches${
                    cotizacionVigente.valorDescuento
                      ? ` · descuento ${cotizacionVigente.porcentajeDescuento}% (−${pesos(cotizacionVigente.valorDescuento)})`
                      : ''
                  }`
                : 'Complete fechas y nacimientos válidos para obtener la cotización vigente.'}
            </small>
          </div>

          {cotizacionVigente && (
            <div style={{ marginBottom: '12px' }}>
              <span className="eyebrow" style={{ marginBottom: '6px' }}>
                DESGLOSE NOCHE POR NOCHE
              </span>
              <div className="night-table">
                {cotizacionVigente.detalleNoches.map((noche) => (
                  <div key={noche.noche} style={{ gridTemplateColumns: '48px 1fr auto', padding: '7px 9px' }}>
                    <b>{fechaCorta(noche.noche)}</b>
                    <small>
                      {nombreTemporada[noche.temporada]} · {noche.ocupantesFacturables} ×{' '}
                      {pesos(noche.tarifaOcupanteNoche)}
                    </small>
                    <strong>{pesos(noche.subtotalNoche)}</strong>
                  </div>
                ))}
              </div>
            </div>
          )}

          {errorCotizacion && (
            <div style={{ marginBottom: '10px' }}>
              <p className="form-error" role="alert">
                <Icono nombre="alerta" tamano={14} /> {errorCotizacion}
              </p>
              <button
                type="button"
                className="secondary compact"
                style={{ marginTop: '6px', width: '100%' }}
                onClick={() => setRecotizarTick((n) => n + 1)}
              >
                Reintentar cotización
              </button>
            </div>
          )}

          {errors.root?.servidor && (
            <p className="form-error" role="alert" style={{ marginBottom: '10px' }}>
              <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
            </p>
          )}

          {sesionVencidaAlEnviar ? (
            <Link className="primary full" to="/login" state={{ desde: '/apartamentos' }}>
              Ingresar nuevamente <Icono nombre="flecha" tamano={18} />
            </Link>
          ) : (
            <button
              type="submit"
              className="primary full"
              disabled={isSubmitting || cotizando || !cotizacionVigente || errorCotizacion !== null}
            >
              {isSubmitting ? 'Creando reserva…' : 'Crear reserva PENDIENTE'} <Icono nombre="flecha" tamano={18} />
            </button>
          )}

          <p className="review-note">
            <Icono nombre="escudo" tamano={14} /> Nace en estado PENDIENTE con el valor y la política congelados. Se
            confirma con el anticipo del 30% en recepción.
          </p>
        </aside>
      </form>
    </section>
  );
}

interface ConfirmacionReservaCreadaProps {
  reserva: ReservaDetalle;
  apartamento: Apartamento;
}

function ConfirmacionReservaCreada({ reserva, apartamento }: ConfirmacionReservaCreadaProps) {
  const [aviso, setAviso] = useState<string | null>(null);
  const cerrarAviso = useCallback(() => setAviso(null), []);

  async function copiarCodigo() {
    try {
      await navigator.clipboard.writeText(reserva.codigo);
      setAviso(`Código ${reserva.codigo} copiado al portapapeles.`);
    } catch {
      setAviso(`Código de reserva: ${reserva.codigo}`);
    }
  }

  return (
    <section>
      <EncabezadoPagina
        antetitulo={`RESERVA REGISTRADA · CANAL ${reserva.canalOrigen}`}
        titulo={reserva.codigo}
        descripcion={`${apartamento.identificacion} · ${apartamento.nombre} · Titular ${reserva.titular.nombreCompleto}`}
      >
        <EtiquetaEstado estado={reserva.estado} />
      </EncabezadoPagina>

      <div className="form-section" style={{ maxWidth: '760px' }}>
        <p className="form-message" role="status">
          <Icono nombre="chequeo" tamano={16} /> Reserva creada exitosamente en estado{' '}
          <strong>{reserva.estado}</strong>. Conserve el código <strong>{reserva.codigo}</strong> para consultarla o
          gestionarla posteriormente.
        </p>

        <div className="folio-summary" style={{ marginTop: '16px' }}>
          <div>
            <small>Código de reserva</small>
            <b>{reserva.codigo}</b>
          </div>
          <div>
            <small>Apartamento</small>
            <b>
              {apartamento.identificacion} · {apartamento.nombre}
            </b>
          </div>
          <div>
            <small>Estancia</small>
            <b>
              {fechaCorta(reserva.fechaEntrada)} → {fechaCorta(reserva.fechaSalida)} (
              {noches(reserva.fechaEntrada, reserva.fechaSalida)} noches)
            </b>
          </div>
          <div>
            <small>Valor total congelado</small>
            <b>{pesos(reserva.cotizacionCongelada.valorTotal)}</b>
          </div>
          <div>
            <small>Ocupantes registrados</small>
            <b>
              {reserva.ocupantes.length} {reserva.ocupantes.length === 1 ? 'persona' : 'personas'}
            </b>
          </div>
          <div>
            <small>Llegada estimada · Mascotas</small>
            <b>
              {hora(reserva.horaEstimadaLlegada)} · {reserva.mascotas}
            </b>
          </div>
        </div>

        <p className="policy-note" style={{ marginBottom: '16px' }}>
          <Icono nombre="escudo" tamano={14} /> La reserva quedó registrada en estado PENDIENTE. Para confirmarla debe
          realizarse el pago del anticipo del 30% en recepción (política versión {reserva.versionPolitica} congelada).
        </p>

        <div className="button-row" style={{ justifyContent: 'flex-start', flexWrap: 'wrap' }}>
          <button type="button" className="primary" onClick={copiarCodigo}>
            <Icono nombre="chequeo" tamano={16} /> Copiar código
          </button>
          <Link className="secondary" to="/apartamentos">
            Volver al catálogo
          </Link>
          <Link className="secondary" to={`/mis-reservas/${reserva.codigo}`}>
            Ver en Mis reservas
          </Link>
        </div>
      </div>

      {aviso && <Aviso mensaje={aviso} onCerrar={cerrarAviso} />}
    </section>
  );
}
