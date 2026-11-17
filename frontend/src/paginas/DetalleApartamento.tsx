import { zodResolver } from '@hookform/resolvers/zod';
import axios from 'axios';
import { useEffect, useMemo, useState } from 'react';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router';
import { z } from 'zod';
import { CalendarioApartamento } from '../componentes/CalendarioApartamento';
import { Cargando } from '../componentes/Cargando';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta } from '../componentes/Etiqueta';
import { Icono } from '../componentes/Icono';
import { fotoDeApartamento } from '../datos/fotos';
import {
  nombreEstadoOperativo,
  nombreTemporada,
  type Apartamento,
} from '../modelos/apartamento';
import type { ErrorResponse } from '../modelos/error';
import type { Cotizacion, CotizacionRequest } from '../modelos/reserva';
import { consultarApartamento, cotizarEstancia } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { hoyISO } from '../utilidades/fechas';
import { fechaCorta, noches, pesos, sumarDias } from '../utilidades/formato';

const esquemaCotizacion = z
  .object({
    fechaEntrada: z.string().min(1, 'Seleccione la fecha de entrada'),
    fechaSalida: z.string().min(1, 'Seleccione la fecha de salida'),
    ocupantes: z
      .array(
        z.object({
          fechaNacimiento: z
            .string()
            .min(1, 'Ingrese la fecha de nacimiento')
            .refine((fecha) => fecha < hoyISO(), 'Debe ser anterior a hoy'),
        }),
      )
      .min(1, 'Agregue al menos un ocupante'),
  })
  .superRefine((datos, ctx) => {
    const hoy = hoyISO();
    if (datos.fechaEntrada && datos.fechaEntrada < hoy) {
      ctx.addIssue({
        code: 'custom',
        path: ['fechaEntrada'],
        message: 'La fecha de entrada no puede ser anterior a hoy',
      });
    }
    if (datos.fechaEntrada && datos.fechaSalida && datos.fechaSalida <= datos.fechaEntrada) {
      ctx.addIssue({
        code: 'custom',
        path: ['fechaSalida'],
        message: 'La fecha de salida debe ser posterior a la de entrada',
      });
    }
  });

type FormularioCotizacion = z.infer<typeof esquemaCotizacion>;

interface SolicitudCotizacion {
  seq: number;
  firma: string;
  request: CotizacionRequest;
}

function leerValoresIniciales(searchParams: URLSearchParams): {
  valores: FormularioCotizacion;
  cotizarAlCargar: boolean;
} {
  const hoy = hoyISO();
  const entradaParam = searchParams.get('fechaEntrada') ?? '';
  const salidaParam = searchParams.get('fechaSalida') ?? '';
  const fechaEntrada =
    /^\d{4}-\d{2}-\d{2}$/.test(entradaParam) && entradaParam >= hoy ? entradaParam : sumarDias(hoy, 1);
  const fechaSalida =
    /^\d{4}-\d{2}-\d{2}$/.test(salidaParam) && salidaParam > fechaEntrada
      ? salidaParam
      : sumarDias(fechaEntrada, 3);

  const ocupantesParam = Number(searchParams.get('totalOcupantes'));
  const totalOcupantes =
    Number.isInteger(ocupantesParam) && ocupantesParam >= 1 && ocupantesParam <= 50 ? ocupantesParam : 1;

  const fechasValidas = searchParams
    .getAll('fechasNacimiento')
    .filter((f) => /^\d{4}-\d{2}-\d{2}$/.test(f) && f < hoy);

  if (fechasValidas.length > 0) {
    const cantidad = Math.max(totalOcupantes, fechasValidas.length);
    const ocupantes = Array.from({ length: cantidad }, (_, i) => ({
      fechaNacimiento: fechasValidas[i] ?? '',
    }));
    const todasCompletas = ocupantes.every((o) => o.fechaNacimiento !== '');
    return {
      valores: { fechaEntrada, fechaSalida, ocupantes },
      cotizarAlCargar: todasCompletas,
    };
  }

  return {
    valores: {
      fechaEntrada,
      fechaSalida,
      ocupantes: Array.from({ length: totalOcupantes }, () => ({ fechaNacimiento: '' })),
    },
    cotizarAlCargar: false,
  };
}

function construirFirmaCotizacion(
  identificacion: string,
  fechaEntrada: string,
  fechaSalida: string,
  ocupantes: { fechaNacimiento: string }[],
): string {
  return JSON.stringify({
    identificacionApartamento: identificacion,
    fechaEntrada,
    fechaSalida,
    ocupantes: ocupantes.map((o) => ({ fechaNacimiento: o.fechaNacimiento })),
  });
}

function esMenorDeDiezAnos(fechaNacimiento: string, fechaEntrada: string): boolean | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(fechaNacimiento) || !/^\d{4}-\d{2}-\d{2}$/.test(fechaEntrada)) {
    return null;
  }
  const [anio, mes, dia] = fechaNacimiento.split('-').map(Number);
  const cumpleDiez = `${String(anio + 10).padStart(4, '0')}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
  return cumpleDiez > fechaEntrada;
}

function formatoErrorBackend(error: unknown): string {
  const base = mensajeDeError(error);
  if (axios.isAxiosError<ErrorResponse>(error) && error.response?.data?.detalles?.length) {
    return `${base} (${error.response.data.detalles.join(' · ')})`;
  }
  return base;
}

function fotoComplementaria(principal: string): string {
  return principal === '/fotos/orquidea.jpg' ? '/fotos/barranquero.jpg' : '/fotos/orquidea.jpg';
}

export default function DetalleApartamento() {
  const { identificacion = '' } = useParams();
  const [searchParams] = useSearchParams();
  const navegar = useNavigate();

  const [inicioFormulario] = useState(() => leerValoresIniciales(searchParams));

  const [estadoApartamento, setEstadoApartamento] = useState<{
    clave: string;
    apartamento: Apartamento | null;
    error: string | null;
  } | null>(null);

  const [solicitudManual, setSolicitudManual] = useState<SolicitudCotizacion | null>(null);
  const [estadoCotizacion, setEstadoCotizacion] = useState<{
    clave: string;
    firma: string;
    cotizacion: Cotizacion | null;
    error: string | null;
  } | null>(null);

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<FormularioCotizacion>({
    resolver: zodResolver(esquemaCotizacion),
    mode: 'onTouched',
    defaultValues: inicioFormulario.valores,
  });

  const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });

  const [entradaObservada, salidaObservada, ocupantesObservados] = useWatch({
    control,
    name: ['fechaEntrada', 'fechaSalida', 'ocupantes'],
  });

  // Consulta del apartamento en el backend al cambiar la identificación de la ruta.
  useEffect(() => {
    if (!identificacion) return;
    const controlador = new AbortController();
    let vigente = true;

    consultarApartamento(identificacion, controlador.signal)
      .then((datos) => {
        if (!vigente) return;
        setEstadoApartamento({ clave: identificacion, apartamento: datos, error: null });
      })
      .catch((error) => {
        if (axios.isCancel(error) || !vigente) return;
        setEstadoApartamento({ clave: identificacion, apartamento: null, error: formatoErrorBackend(error) });
      });

    return () => {
      vigente = false;
      controlador.abort();
    };
  }, [identificacion]);

  const respuestaApartamento = estadoApartamento?.clave === identificacion ? estadoApartamento : null;
  const cargandoApartamento = Boolean(identificacion) && respuestaApartamento === null;
  const apartamento = respuestaApartamento?.apartamento ?? null;
  const errorApartamento = respuestaApartamento?.error ?? null;

  // Si llegaron fechas de nacimiento completas desde Fase 1, prepara la cotización automática inicial.
  const solicitudInicial = useMemo<SolicitudCotizacion | null>(() => {
    if (!inicioFormulario.cotizarAlCargar || !apartamento) return null;
    if (inicioFormulario.valores.ocupantes.length > apartamento.capacidad) return null;
    const request: CotizacionRequest = {
      identificacionApartamento: apartamento.identificacion,
      fechaEntrada: inicioFormulario.valores.fechaEntrada,
      fechaSalida: inicioFormulario.valores.fechaSalida,
      ocupantes: inicioFormulario.valores.ocupantes.map((o) => ({ fechaNacimiento: o.fechaNacimiento })),
    };
    return {
      seq: 0,
      firma: construirFirmaCotizacion(
        apartamento.identificacion,
        request.fechaEntrada,
        request.fechaSalida,
        request.ocupantes,
      ),
      request,
    };
  }, [apartamento, inicioFormulario]);

  const solicitudEfectiva = solicitudManual ?? solicitudInicial;

  useEffect(() => {
    if (!solicitudEfectiva) return;
    const controlador = new AbortController();
    let vigente = true;
    const { seq, firma, request } = solicitudEfectiva;
    const clave = `${seq}|${firma}`;

    cotizarEstancia(request, controlador.signal)
      .then((cotizacion) => {
        if (!vigente) return;
        setEstadoCotizacion({ clave, firma, cotizacion, error: null });
      })
      .catch((error) => {
        if (axios.isCancel(error) || !vigente) return;
        setEstadoCotizacion({ clave, firma, cotizacion: null, error: formatoErrorBackend(error) });
      });

    return () => {
      vigente = false;
      controlador.abort();
    };
  }, [solicitudEfectiva]);

  const firmaActual = construirFirmaCotizacion(
    identificacion,
    entradaObservada,
    salidaObservada,
    ocupantesObservados,
  );
  const claveSolicitud = solicitudEfectiva ? `${solicitudEfectiva.seq}|${solicitudEfectiva.firma}` : null;
  const cotizando = claveSolicitud !== null && estadoCotizacion?.clave !== claveSolicitud;
  const cotizacionCoincidente = estadoCotizacion?.firma === firmaActual ? estadoCotizacion : null;
  const cotizacionVigente = cotizacionCoincidente?.cotizacion ?? null;
  const errorCotizacion = cotizacionCoincidente?.error ?? null;

  const capacidadExcedida = apartamento ? fields.length > apartamento.capacidad : false;
  const capacidadCompleta = apartamento ? fields.length >= apartamento.capacidad : false;
  const faltanFechasNacimiento = ocupantesObservados.some((o) => !o.fechaNacimiento);

  function alCotizar(datos: FormularioCotizacion) {
    if (!apartamento || cotizando || datos.ocupantes.length > apartamento.capacidad) return;

    const request: CotizacionRequest = {
      identificacionApartamento: apartamento.identificacion,
      fechaEntrada: datos.fechaEntrada,
      fechaSalida: datos.fechaSalida,
      ocupantes: datos.ocupantes.map((o) => ({ fechaNacimiento: o.fechaNacimiento })),
    };
    const firmaEnvio = construirFirmaCotizacion(
      apartamento.identificacion,
      datos.fechaEntrada,
      datos.fechaSalida,
      datos.ocupantes,
    );
    setSolicitudManual((actual) => ({
      seq: (actual?.seq ?? 0) + 1,
      firma: firmaEnvio,
      request,
    }));
  }

  function continuarAReserva() {
    if (!apartamento || !apartamento.activo || !cotizacionVigente) return;
    const params = new URLSearchParams();
    params.set('apartamento', apartamento.identificacion);
    params.set('fechaEntrada', entradaObservada);
    params.set('fechaSalida', salidaObservada);
    params.set('totalOcupantes', String(ocupantesObservados.length));
    for (const o of ocupantesObservados) {
      params.append('fechasNacimiento', o.fechaNacimiento);
    }

    navegar(`/reservas/nueva?${params.toString()}`, {
      state: {
        identificacionApartamento: apartamento.identificacion,
        nombreApartamento: apartamento.nombre,
        fechaEntrada: entradaObservada,
        fechaSalida: salidaObservada,
        totalOcupantes: ocupantesObservados.length,
        fechasNacimiento: ocupantesObservados.map((o) => o.fechaNacimiento),
        cotizacion: cotizacionVigente,
      },
    });
  }

  const volverUrl = searchParams.toString() ? `/apartamentos?${searchParams.toString()}` : '/apartamentos';

  if (cargandoApartamento) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="DETALLE DEL APARTAMENTO"
          titulo={identificacion || 'Apartamento'}
          descripcion="Consultando ficha técnica, tarifas y calendario en el servidor…"
        >
          <Link className="secondary" to={volverUrl}>
            Volver al catálogo
          </Link>
        </EncabezadoPagina>
        <Cargando filas={5} texto="Cargando información del apartamento…" />
      </section>
    );
  }

  if (errorApartamento || !apartamento) {
    return (
      <section>
        <EncabezadoPagina
          antetitulo="APARTAMENTO NO DISPONIBLE"
          titulo={identificacion || 'Unidad no encontrada'}
          descripcion="No fue posible consultar el apartamento solicitado."
        >
          <Link className="secondary" to={volverUrl}>
            Volver al catálogo
          </Link>
        </EncabezadoPagina>

        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} />{' '}
          {errorApartamento ?? `No existe el apartamento ${identificacion}.`}
        </p>

        <div className="empty">
          <Icono nombre="alerta" tamano={28} />
          <h3>Apartamento no encontrado</h3>
          <p style={{ marginTop: '6px' }}>
            Verifique el código en el catálogo de disponibilidad de Siete Raíces.
          </p>
          <div className="button-row" style={{ justifyContent: 'center', marginTop: '14px' }}>
            <Link className="primary compact" to={volverUrl}>
              Volver a apartamentos
            </Link>
          </div>
        </div>
      </section>
    );
  }

  const fotoPrincipal = fotoDeApartamento(apartamento.identificacion);
  const fotoSecundaria = fotoComplementaria(fotoPrincipal);
  const hoy = hoyISO();
  const ayer = sumarDias(hoy, -1);
  const totalNochesFormulario =
    entradaObservada && salidaObservada && salidaObservada > entradaObservada
      ? noches(entradaObservada, salidaObservada)
      : 0;
  const unidadVendible = apartamento.activo;
  const facturablesBackend = cotizacionVigente?.detalleNoches[0]?.ocupantesFacturables;

  return (
    <section>
      <EncabezadoPagina
        antetitulo={`${apartamento.identificacion} · APARTAMENTO COMPLETO`}
        titulo={apartamento.nombre}
        descripcion="Ficha técnica, tarifas por temporada, calendario de disponibilidad y cotización oficial."
      >
        <Link className="secondary" to={volverUrl}>
          Volver a disponibilidad
        </Link>
      </EncabezadoPagina>

      <div
        className="form-section"
        style={{ padding: 0, overflow: 'hidden', background: 'var(--cream)' }}
      >
        <div className="detail-gallery">
          <img
            className="gallery-main"
            src={fotoPrincipal}
            alt={`Apartamento ${apartamento.identificacion} ${apartamento.nombre}`}
          />
          <img
            src={fotoSecundaria}
            alt={`Vista interior de ${apartamento.identificacion} ${apartamento.nombre}`}
          />
          <span className="gallery-count">
            Siete Raíces · {apartamento.identificacion}
          </span>
        </div>

        <div className="detail-content">
          <div className="detail-intro">
            <div>
              <span className="eyebrow">{apartamento.identificacion} · APARTAMENTO COMPLETO</span>
              <h2>{apartamento.nombre}</h2>
              <p>{apartamento.descripcion}</p>
            </div>

            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              {!apartamento.activo ? (
                <Etiqueta tono="danger">
                  <i /> Inactivo (no vendible)
                </Etiqueta>
              ) : apartamento.estadoOperativo === 'FUERA_DE_SERVICIO' ? (
                <Etiqueta tono="danger">
                  <i /> {nombreEstadoOperativo[apartamento.estadoOperativo]}
                </Etiqueta>
              ) : (
                <Etiqueta tono="available">
                  <i /> {nombreEstadoOperativo[apartamento.estadoOperativo]}
                </Etiqueta>
              )}
            </div>
          </div>

          {!apartamento.activo && (
            <p className="form-error bloque" role="alert" style={{ marginTop: '16px', marginBottom: 0 }}>
              <Icono nombre="alerta" tamano={16} /> Esta unidad se encuentra inactiva y no es vendible en este momento.
              No es posible continuar con una reserva.
            </p>
          )}

          {apartamento.activo && apartamento.estadoOperativo === 'FUERA_DE_SERVICIO' && (
            <p className="form-error bloque" role="status" style={{ marginTop: '16px', marginBottom: 0 }}>
              <Icono nombre="alerta" tamano={16} /> Aviso operativo: este apartamento está reportado actualmente como
              fuera de servicio. Consulte el calendario y las fechas disponibles antes de reservar.
            </p>
          )}

          <div className="detail-layout">
            <div>
              <h4 style={{ marginTop: 0 }}>Lo que incluye</h4>
              <div className="amenities">
                <span>
                  <Icono nombre="cama" tamano={15} /> {apartamento.dormitorios}{' '}
                  {apartamento.dormitorios === 1 ? 'dormitorio' : 'dormitorios'}
                </span>
                <span>
                  <Icono nombre="usuarios" tamano={15} /> Hasta {apartamento.capacidad}{' '}
                  {apartamento.capacidad === 1 ? 'persona' : 'personas'}
                </span>
                {apartamento.caracteristicas.map((caracteristica) => (
                  <span key={caracteristica}>
                    <Icono nombre="chequeo" tamano={15} /> {caracteristica}
                  </span>
                ))}
              </div>

              <h4>Tarifas vigentes por ocupante y noche</h4>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                  gap: '8px',
                }}
              >
                {apartamento.tarifas.map((tarifa) => (
                  <div
                    key={tarifa.temporada}
                    style={{
                      background: '#fff',
                      border: '1px solid var(--line)',
                      borderRadius: '8px',
                      padding: '10px 12px',
                    }}
                  >
                    <span className="eyebrow">TEMPORADA {nombreTemporada[tarifa.temporada].toUpperCase()}</span>
                    <strong style={{ display: 'block', font: '750 14px "Manrope"', marginTop: '4px' }}>
                      {pesos(tarifa.valorPorOcupanteNoche)}
                    </strong>
                    <small style={{ fontSize: '9px', color: 'var(--muted)' }}>por ocupante / noche</small>
                  </div>
                ))}
              </div>

              <CalendarioApartamento identificacion={apartamento.identificacion} />

              <h4>Desglose noche por noche</h4>
              {cotizacionVigente ? (
                <div className="night-table">
                  {cotizacionVigente.detalleNoches.map((item) => (
                    <div key={item.noche}>
                      <b>{fechaCorta(item.noche)}</b>
                      <small>Temporada {nombreTemporada[item.temporada].toLowerCase()}</small>
                      <span>
                        {item.ocupantesFacturables} × {pesos(item.tarifaOcupanteNoche)}
                      </span>
                      <strong>{pesos(item.subtotalNoche)}</strong>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty" style={{ padding: '22px' }}>
                  <Icono nombre="billetera" tamano={22} />
                  <h3>Cotización pendiente</h3>
                  <p style={{ marginTop: '4px' }}>
                    Complete las fechas y el nacimiento de cada ocupante en el panel lateral y pulse «Cotizar estancia»
                    para obtener el desglose oficial noche por noche.
                  </p>
                </div>
              )}
            </div>

            <aside className="booking-box" aria-label="Cotizador oficial de estancia" style={{ alignSelf: 'start' }}>
              <span className="eyebrow">TU COTIZACIÓN OFICIAL</span>

              <form onSubmit={handleSubmit(alCotizar)} noValidate>
                <div className="mini-dates">
                  <div>
                    <label htmlFor="cot-entrada">
                      <small>ENTRADA</small>
                    </label>
                    <input
                      id="cot-entrada"
                      type="date"
                      min={hoy}
                      aria-invalid={errors.fechaEntrada ? true : undefined}
                      style={{
                        width: '100%',
                        border: 0,
                        background: 'transparent',
                        fontSize: '11px',
                        fontWeight: 700,
                        color: 'var(--ink)',
                        marginTop: '3px',
                        padding: 0,
                      }}
                      {...register('fechaEntrada')}
                    />
                  </div>
                  <div>
                    <label htmlFor="cot-salida">
                      <small>SALIDA</small>
                    </label>
                    <input
                      id="cot-salida"
                      type="date"
                      min={entradaObservada || hoy}
                      aria-invalid={errors.fechaSalida ? true : undefined}
                      style={{
                        width: '100%',
                        border: 0,
                        background: 'transparent',
                        fontSize: '11px',
                        fontWeight: 700,
                        color: 'var(--ink)',
                        marginTop: '3px',
                        padding: 0,
                      }}
                      {...register('fechaSalida')}
                    />
                  </div>
                </div>

                {errors.fechaEntrada && (
                  <p className="error" role="alert">
                    {errors.fechaEntrada.message}
                  </p>
                )}
                {errors.fechaSalida && (
                  <p className="error" role="alert">
                    {errors.fechaSalida.message}
                  </p>
                )}

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    margin: '10px 0 6px',
                  }}
                >
                  <span className="eyebrow">
                    OCUPANTES ({fields.length} / {apartamento.capacidad})
                  </span>
                  <span style={{ fontSize: '9px', color: 'var(--muted)' }}>
                    {totalNochesFormulario > 0
                      ? `${totalNochesFormulario} ${totalNochesFormulario === 1 ? 'noche' : 'noches'}`
                      : 'Fechas por definir'}
                  </span>
                </div>

                <div style={{ display: 'grid', gap: '8px', marginBottom: '10px' }}>
                  {fields.map((campo, indice) => {
                    const fechaActual = ocupantesObservados[indice]?.fechaNacimiento ?? '';
                    const menorDiez = esMenorDeDiezAnos(fechaActual, entradaObservada);
                    const errorCampo = errors.ocupantes?.[indice]?.fechaNacimiento?.message;

                    return (
                      <div
                        key={campo.id}
                        style={{
                          padding: '9px 10px',
                          borderRadius: '8px',
                          border: '1px solid var(--line)',
                          background: '#f9faf8',
                          display: 'grid',
                          gap: '5px',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <label
                            htmlFor={`ocupante-nacimiento-${indice}`}
                            style={{ fontSize: '9px', fontWeight: 800, color: '#53615a' }}
                          >
                            OCUPANTE {indice + 1} · NACIMIENTO
                          </label>
                          {fields.length > 1 && (
                            <button
                              type="button"
                              onClick={() => remove(indice)}
                              style={{
                                border: 0,
                                background: 'transparent',
                                color: 'var(--red)',
                                fontSize: '9px',
                                fontWeight: 700,
                                padding: 0,
                              }}
                            >
                              Quitar
                            </button>
                          )}
                        </div>
                        <input
                          id={`ocupante-nacimiento-${indice}`}
                          type="date"
                          max={ayer}
                          aria-invalid={errorCampo ? true : undefined}
                          style={{
                            width: '100%',
                            height: '34px',
                            padding: '0 8px',
                            border: '1px solid #d5dcd8',
                            borderRadius: '6px',
                            background: '#fff',
                            fontSize: '10px',
                            color: 'var(--ink)',
                          }}
                          {...register(`ocupantes.${indice}.fechaNacimiento`)}
                        />
                        {errorCampo && (
                          <span className="error" role="alert">
                            {errorCampo}
                          </span>
                        )}
                        {menorDiez === true && (
                          <small style={{ fontSize: '9px', color: 'var(--green)', fontWeight: 600 }}>
                            Menor de 10 años al ingresar (sin cargo según RN-06)
                          </small>
                        )}
                        {menorDiez === false && (
                          <small style={{ fontSize: '9px', color: 'var(--muted)' }}>
                            Ocupante facturable (≥ 10 años al ingresar)
                          </small>
                        )}
                      </div>
                    );
                  })}
                </div>

                {capacidadExcedida && (
                  <p className="form-error" role="alert" style={{ marginBottom: '10px' }}>
                    <Icono nombre="alerta" tamano={14} /> El grupo excede la capacidad máxima de{' '}
                    {apartamento.capacidad} personas.
                  </p>
                )}

                <button
                  type="button"
                  className="add-occupant"
                  onClick={() => append({ fechaNacimiento: '' })}
                  disabled={capacidadCompleta || capacidadExcedida}
                  style={{ marginTop: 0, marginBottom: '12px' }}
                >
                  <Icono nombre="mas" tamano={14} />{' '}
                  {capacidadCompleta
                    ? `Capacidad máxima (${apartamento.capacidad} personas)`
                    : 'Agregar ocupante'}
                </button>

                {faltanFechasNacimiento && !cotizacionVigente && (
                  <p className="hint" style={{ marginTop: 0, marginBottom: '10px' }}>
                    <Icono nombre="reloj" tamano={14} /> Complete todas las fechas de nacimiento para solicitar la
                    cotización oficial al servidor.
                  </p>
                )}

                <button
                  type="submit"
                  className="secondary full"
                  disabled={cotizando || capacidadExcedida}
                  style={{ marginBottom: '12px' }}
                >
                  <Icono nombre="billetera" tamano={16} />{' '}
                  {cotizando ? 'Cotizando en servidor…' : 'Cotizar estancia'}
                </button>
              </form>

              {errorCotizacion && (
                <p className="form-error" role="alert" style={{ marginBottom: '12px' }}>
                  <Icono nombre="alerta" tamano={14} /> {errorCotizacion}
                </p>
              )}

              {cotizacionVigente && (
                <div aria-live="polite">
                  <div className="booking-line">
                    <span>
                      Subtotal ({cotizacionVigente.totalNoches}{' '}
                      {cotizacionVigente.totalNoches === 1 ? 'noche' : 'noches'})
                    </span>
                    <b>{pesos(cotizacionVigente.subtotal)}</b>
                  </div>
                  <div className="booking-line">
                    <span>Ocupantes facturables</span>
                    <b>
                      {facturablesBackend ?? 0} de {ocupantesObservados.length}
                    </b>
                  </div>
                  {(cotizacionVigente.porcentajeDescuento > 0 || cotizacionVigente.valorDescuento > 0) && (
                    <div className="booking-line" style={{ color: 'var(--green)' }}>
                      <span>Descuento estancia prolongada ({cotizacionVigente.porcentajeDescuento}%)</span>
                      <b style={{ color: 'var(--green)' }}>− {pesos(cotizacionVigente.valorDescuento)}</b>
                    </div>
                  )}
                  <div className="booking-total">
                    <span>Total</span>
                    <strong>{pesos(cotizacionVigente.valorTotal)}</strong>
                  </div>
                </div>
              )}

              {!cotizacionVigente && estadoCotizacion !== null && !errorCotizacion && (
                <p className="hint" style={{ marginTop: 0, marginBottom: '12px' }}>
                  <Icono nombre="alerta" tamano={14} /> Los datos cambiaron: vuelva a cotizar para habilitar la
                  reserva.
                </p>
              )}

              <button
                type="button"
                className="primary full"
                disabled={!unidadVendible || cotizacionVigente === null || cotizando}
                onClick={continuarAReserva}
              >
                Continuar con la reserva <Icono nombre="flecha" tamano={18} />
              </button>

              <p className="policy-note" style={{ marginTop: '10px' }}>
                <Icono nombre="escudo" tamano={14} /> Nace PENDIENTE. Se confirma posteriormente con anticipo del 30%.
              </p>
            </aside>
          </div>
        </div>
      </div>
    </section>
  );
}
