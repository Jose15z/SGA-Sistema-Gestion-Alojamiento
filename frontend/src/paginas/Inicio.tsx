import { zodResolver } from '@hookform/resolvers/zod';
import axios from 'axios';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useSearchParams } from 'react-router';
import { z } from 'zod';
import { Cargando } from '../componentes/Cargando';
import { Etiqueta } from '../componentes/Etiqueta';
import { Icono } from '../componentes/Icono';
import { TarjetaApartamento } from '../componentes/TarjetaApartamento';
import {
  nombreUnidadCobro,
  type Alojamiento,
  type ApartamentoDisponible,
  type FiltroDisponibles,
  type PoliticaCancelacion,
} from '../modelos/apartamento';
import type { ErrorResponse } from '../modelos/error';
import type { Pagina } from '../modelos/reserva';
import {
  buscarApartamentosDisponibles,
  consultarAlojamiento,
  consultarPoliticaVigente,
} from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { hoyISO } from '../utilidades/fechas';
import { hora, noches, pesos, sumarDias } from '../utilidades/formato';

const TAMANO_PAGINA = 10;

// Características reales registradas en los apartamentos de Siete Raíces (Ficha A.2).
const CARACTERISTICAS_OPCIONES = [
  'Balcón',
  'Balcón panorámico',
  'Baño privado',
  'Cama doble',
  'Cocina equipada',
  'Dos baños',
  'Espacio de trabajo',
  'Sala',
  'Sala amplia',
  'Sofá cama',
  'Zona de descanso',
] as const;

const enteroOpcional = (etiqueta: string) =>
  z
    .string()
    .trim()
    .refine(
      (valor) => valor === '' || (/^\d+$/.test(valor) && Number(valor) >= 1),
      `${etiqueta} debe ser un número entero mayor o igual a 1`,
    );

const montoOpcional = (etiqueta: string) =>
  z
    .string()
    .trim()
    .refine(
      (valor) => valor === '' || (!Number.isNaN(Number(valor)) && Number(valor) >= 0),
      `${etiqueta} debe ser un valor numérico mayor o igual a 0`,
    );

const esquemaBusqueda = z
  .object({
    fechaEntrada: z.string().min(1, 'Seleccione la fecha de entrada'),
    fechaSalida: z.string().min(1, 'Seleccione la fecha de salida'),
    totalOcupantes: z
      .number({ error: 'Ingrese el número de ocupantes' })
      .int('Debe ser un número entero')
      .min(1, 'Mínimo 1 ocupante')
      .max(50, 'Máximo 50 ocupantes'),
    caracteristicaPrincipal: z.string(),
    caracteristicasAdicionales: z.array(z.string()).max(10, 'Máximo 10 características'),
    capacidadMinima: enteroOpcional('La capacidad mínima'),
    dormitoriosMinimos: enteroOpcional('Los dormitorios mínimos'),
    precioMinimo: montoOpcional('El precio mínimo'),
    precioMaximo: montoOpcional('El precio máximo'),
    incluirEdades: z.boolean(),
    fechasNacimiento: z.array(z.string()),
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

    if (
      datos.precioMinimo.trim() !== '' &&
      datos.precioMaximo.trim() !== '' &&
      Number(datos.precioMinimo) > Number(datos.precioMaximo)
    ) {
      ctx.addIssue({
        code: 'custom',
        path: ['precioMaximo'],
        message: 'El precio máximo no puede ser menor al mínimo',
      });
    }

    if (datos.incluirEdades) {
      for (let i = 0; i < datos.totalOcupantes; i++) {
        const fecha = datos.fechasNacimiento[i] ?? '';
        if (!fecha || fecha >= hoy) {
          ctx.addIssue({
            code: 'custom',
            path: ['fechasNacimiento', i],
            message: 'Indique una fecha anterior a hoy',
          });
        }
      }
    }
  });

type FormularioBusqueda = z.infer<typeof esquemaBusqueda>;

function combinarCaracteristicas(principal: string, adicionales: string[]): string[] {
  const conjunto = new Set<string>();
  if (principal.trim()) conjunto.add(principal.trim());
  for (const item of adicionales) {
    if (item.trim()) conjunto.add(item.trim());
  }
  return Array.from(conjunto).slice(0, 10);
}

function construirFiltro(datos: FormularioBusqueda, page: number): FiltroDisponibles {
  const caracteristicas = combinarCaracteristicas(
    datos.caracteristicaPrincipal,
    datos.caracteristicasAdicionales,
  );
  const filtro: FiltroDisponibles = {
    fechaEntrada: datos.fechaEntrada,
    fechaSalida: datos.fechaSalida,
    totalOcupantes: datos.totalOcupantes,
    page,
    size: TAMANO_PAGINA,
  };

  if (datos.capacidadMinima.trim() !== '') filtro.capacidadMinima = Number(datos.capacidadMinima);
  if (datos.dormitoriosMinimos.trim() !== '') filtro.dormitoriosMinimos = Number(datos.dormitoriosMinimos);
  if (datos.precioMinimo.trim() !== '') filtro.precioMinimo = Number(datos.precioMinimo);
  if (datos.precioMaximo.trim() !== '') filtro.precioMaximo = Number(datos.precioMaximo);
  if (caracteristicas.length > 0) filtro.caracteristicas = caracteristicas;
  if (datos.incluirEdades) {
    filtro.fechasNacimiento = datos.fechasNacimiento.slice(0, datos.totalOcupantes);
  }

  return filtro;
}

function leerEstadoInicial(searchParams: URLSearchParams): {
  valores: FormularioBusqueda;
  filtro: FiltroDisponibles;
} {
  const hoy = hoyISO();
  const entradaParam = searchParams.get('fechaEntrada') ?? '';
  const salidaParam = searchParams.get('fechaSalida') ?? '';
  const fechaEntrada = /^\d{4}-\d{2}-\d{2}$/.test(entradaParam) && entradaParam >= hoy ? entradaParam : sumarDias(hoy, 1);
  const fechaSalida =
    /^\d{4}-\d{2}-\d{2}$/.test(salidaParam) && salidaParam > fechaEntrada
      ? salidaParam
      : sumarDias(fechaEntrada, 3);

  const ocupantesNum = Number(searchParams.get('totalOcupantes'));
  const totalOcupantes =
    Number.isInteger(ocupantesNum) && ocupantesNum >= 1 && ocupantesNum <= 50 ? ocupantesNum : 2;

  const caracteristicas = searchParams
    .getAll('caracteristicas')
    .map((c) => c.trim())
    .filter(Boolean)
    .slice(0, 10);
  const [caracteristicaPrincipal = '', ...caracteristicasAdicionales] = caracteristicas;

  const capParam = searchParams.get('capacidadMinima') ?? '';
  const dormParam = searchParams.get('dormitoriosMinimos') ?? '';
  const minParam = searchParams.get('precioMinimo') ?? '';
  const maxParam = searchParams.get('precioMaximo') ?? '';

  const capacidadMinima = /^\d+$/.test(capParam) && Number(capParam) >= 1 ? capParam : '';
  const dormitoriosMinimos = /^\d+$/.test(dormParam) && Number(dormParam) >= 1 ? dormParam : '';
  const precioMinimo = minParam !== '' && !Number.isNaN(Number(minParam)) && Number(minParam) >= 0 ? minParam : '';
  const precioMaximo = maxParam !== '' && !Number.isNaN(Number(maxParam)) && Number(maxParam) >= 0 ? maxParam : '';

  const fechasParam = searchParams
    .getAll('fechasNacimiento')
    .filter((f) => /^\d{4}-\d{2}-\d{2}$/.test(f) && f < hoy);
  const incluirEdades = fechasParam.length === totalOcupantes;
  const fechasNacimiento = incluirEdades ? fechasParam : Array.from({ length: totalOcupantes }, () => '');

  const paginaNum = Number(searchParams.get('page'));
  const page = Number.isInteger(paginaNum) && paginaNum >= 0 ? paginaNum : 0;

  const valores: FormularioBusqueda = {
    fechaEntrada,
    fechaSalida,
    totalOcupantes,
    caracteristicaPrincipal,
    caracteristicasAdicionales,
    capacidadMinima,
    dormitoriosMinimos,
    precioMinimo,
    precioMaximo,
    incluirEdades,
    fechasNacimiento,
  };

  return { valores, filtro: construirFiltro(valores, page) };
}

function serializarFiltroEnUrl(filtro: FiltroDisponibles): URLSearchParams {
  const params = new URLSearchParams();
  params.set('fechaEntrada', filtro.fechaEntrada);
  params.set('fechaSalida', filtro.fechaSalida);
  params.set('totalOcupantes', String(filtro.totalOcupantes));
  if (filtro.capacidadMinima !== undefined) params.set('capacidadMinima', String(filtro.capacidadMinima));
  if (filtro.dormitoriosMinimos !== undefined) params.set('dormitoriosMinimos', String(filtro.dormitoriosMinimos));
  if (filtro.precioMinimo !== undefined) params.set('precioMinimo', String(filtro.precioMinimo));
  if (filtro.precioMaximo !== undefined) params.set('precioMaximo', String(filtro.precioMaximo));
  for (const c of filtro.caracteristicas ?? []) params.append('caracteristicas', c);
  for (const f of filtro.fechasNacimiento ?? []) params.append('fechasNacimiento', f);
  if ((filtro.page ?? 0) > 0) params.set('page', String(filtro.page));
  return params;
}

function formatoErrorBackend(error: unknown): string {
  const base = mensajeDeError(error);
  if (axios.isAxiosError<ErrorResponse>(error) && error.response?.data?.detalles?.length) {
    return `${base} (${error.response.data.detalles.join(' · ')})`;
  }
  return base;
}

function textoAntelacion(horas: number): string {
  if (horas <= 0) return 'Menos de 48 horas antes de la entrada';
  const dias = Math.floor(horas / 24);
  return dias >= 1 && horas % 24 === 0
    ? `${dias} día${dias === 1 ? '' : 's'} o más antes (${horas} h)`
    : `${horas} h o más antes`;
}

export default function Inicio() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [estadoInicial] = useState(() => leerEstadoInicial(searchParams));

  const [alojamiento, setAlojamiento] = useState<Alojamiento | null>(null);
  const [politica, setPolitica] = useState<PoliticaCancelacion | null>(null);
  const [errorAlojamiento, setErrorAlojamiento] = useState<string | null>(null);

  const [filtroActivo, setFiltroActivo] = useState<FiltroDisponibles>(estadoInicial.filtro);
  const claveFiltro = useMemo(() => JSON.stringify(filtroActivo), [filtroActivo]);
  const [estadoBusqueda, setEstadoBusqueda] = useState<{
    clave: string;
    pagina: Pagina<ApartamentoDisponible> | null;
    error: string | null;
  } | null>(null);

  const [mostrarInfo, setMostrarInfo] = useState(false);
  const [mostrarFiltrosAvanzados, setMostrarFiltrosAvanzados] = useState(() => {
    const f = estadoInicial.filtro;
    return Boolean(
      f.capacidadMinima !== undefined ||
        f.dormitoriosMinimos !== undefined ||
        f.precioMinimo !== undefined ||
        f.precioMaximo !== undefined ||
        f.fechasNacimiento?.length ||
        (f.caracteristicas?.length ?? 0) > 1,
    );
  });

  const solicitudIdRef = useRef(0);

  const {
    register,
    control,
    handleSubmit,
    setValue,
    getValues,
    reset,
    formState: { errors },
  } = useForm<FormularioBusqueda>({
    resolver: zodResolver(esquemaBusqueda),
    mode: 'onTouched',
    defaultValues: estadoInicial.valores,
  });

  const [
    entradaObservada,
    ocupantesObservados,
    incluirEdadesObservado,
    caracteristicasAdicionalesObservadas,
    capacidadObservada,
    dormitoriosObservados,
    precioMinObservado,
    precioMaxObservado,
  ] = useWatch({
    control,
    name: [
      'fechaEntrada',
      'totalOcupantes',
      'incluirEdades',
      'caracteristicasAdicionales',
      'capacidadMinima',
      'dormitoriosMinimos',
      'precioMinimo',
      'precioMaximo',
    ],
  });

  // Carga inicial de la identidad del alojamiento y política de cancelación vigente.
  useEffect(() => {
    const controlador = new AbortController();
    let vigente = true;

    Promise.allSettled([
      consultarAlojamiento(controlador.signal),
      consultarPoliticaVigente(controlador.signal),
    ]).then(([resAlojamiento, resPolitica]) => {
      if (!vigente) return;

      if (resAlojamiento.status === 'fulfilled') {
        setAlojamiento(resAlojamiento.value);
        setErrorAlojamiento(null);
      } else if (!axios.isCancel(resAlojamiento.reason)) {
        setErrorAlojamiento(mensajeDeError(resAlojamiento.reason));
      }

      if (resPolitica.status === 'fulfilled') {
        setPolitica(resPolitica.value);
      } else if (!axios.isCancel(resPolitica.reason) && resAlojamiento.status === 'fulfilled') {
        setErrorAlojamiento(mensajeDeError(resPolitica.reason));
      }
    });

    return () => {
      vigente = false;
      controlador.abort();
    };
  }, []);

  // Consulta de disponibilidad en el backend con cancelación y control contra respuestas fuera de orden.
  useEffect(() => {
    const idSolicitud = ++solicitudIdRef.current;
    const controlador = new AbortController();
    let vigente = true;
    const solicitud: FiltroDisponibles = JSON.parse(claveFiltro);

    buscarApartamentosDisponibles(solicitud, controlador.signal)
      .then((resultado) => {
        if (!vigente || solicitudIdRef.current !== idSolicitud) return;
        setEstadoBusqueda({ clave: claveFiltro, pagina: resultado, error: null });
      })
      .catch((error) => {
        if (axios.isCancel(error) || !vigente || solicitudIdRef.current !== idSolicitud) return;
        setEstadoBusqueda({ clave: claveFiltro, pagina: null, error: formatoErrorBackend(error) });
      });

    return () => {
      vigente = false;
      controlador.abort();
    };
  }, [claveFiltro]);

  // Solo se muestran los resultados que corresponden exactamente al filtro vigente.
  const respuestaVigente = estadoBusqueda?.clave === claveFiltro ? estadoBusqueda : null;
  const cargandoResultados = respuestaVigente === null;
  const pagina = respuestaVigente?.pagina ?? null;
  const errorResultados = respuestaVigente?.error ?? null;

  function sincronizarArregloEdades(cantidadRaw: number) {
    const cantidad = Number.isInteger(cantidadRaw) ? Math.min(Math.max(cantidadRaw, 1), 50) : 1;
    const actuales = getValues('fechasNacimiento');
    if (actuales.length === cantidad) return;
    setValue(
      'fechasNacimiento',
      Array.from({ length: cantidad }, (_, i) => actuales[i] ?? ''),
      { shouldValidate: false },
    );
  }

  function alBuscar(datos: FormularioBusqueda) {
    const nuevoFiltro = construirFiltro(datos, 0);
    setEstadoBusqueda(null);
    setFiltroActivo(nuevoFiltro);
    setSearchParams(serializarFiltroEnUrl(nuevoFiltro), { replace: true });
  }

  function cambiarPagina(nuevaPagina: number) {
    if (nuevaPagina < 0 || cargandoResultados) return;
    const nuevoFiltro: FiltroDisponibles = { ...filtroActivo, page: nuevaPagina };
    setEstadoBusqueda(null);
    setFiltroActivo(nuevoFiltro);
    setSearchParams(serializarFiltroEnUrl(nuevoFiltro), { replace: true });
  }

  function restablecerFiltros() {
    const hoy = hoyISO();
    const entradaDefecto = sumarDias(hoy, 1);
    const salidaDefecto = sumarDias(hoy, 4);
    const valoresPorDefecto: FormularioBusqueda = {
      fechaEntrada: entradaDefecto,
      fechaSalida: salidaDefecto,
      totalOcupantes: 2,
      caracteristicaPrincipal: '',
      caracteristicasAdicionales: [],
      capacidadMinima: '',
      dormitoriosMinimos: '',
      precioMinimo: '',
      precioMaximo: '',
      incluirEdades: false,
      fechasNacimiento: ['', ''],
    };
    reset(valoresPorDefecto);
    const filtroLimpio = construirFiltro(valoresPorDefecto, 0);
    setEstadoBusqueda(null);
    setFiltroActivo(filtroLimpio);
    setSearchParams(serializarFiltroEnUrl(filtroLimpio), { replace: true });
  }

  function alternarCaracteristicaAdicional(caracteristica: string) {
    const actual = caracteristicasAdicionalesObservadas;
    const siguiente = actual.includes(caracteristica)
      ? actual.filter((item) => item !== caracteristica)
      : [...actual, caracteristica];
    setValue('caracteristicasAdicionales', siguiente.slice(0, 10), { shouldValidate: true });
  }

  const hoy = hoyISO();
  const ayer = sumarDias(hoy, -1);
  const nochesConsulta =
    filtroActivo.fechaEntrada && filtroActivo.fechaSalida && filtroActivo.fechaSalida > filtroActivo.fechaEntrada
      ? noches(filtroActivo.fechaEntrada, filtroActivo.fechaSalida)
      : 0;

  const conteoFiltrosAvanzados =
    (capacidadObservada.trim() ? 1 : 0) +
    (dormitoriosObservados.trim() ? 1 : 0) +
    (precioMinObservado.trim() ? 1 : 0) +
    (precioMaxObservado.trim() ? 1 : 0) +
    (caracteristicasAdicionalesObservadas.length > 0 ? 1 : 0) +
    (incluirEdadesObservado ? 1 : 0);

  const cantidadOcupantesEdades = Number.isInteger(ocupantesObservados)
    ? Math.min(Math.max(ocupantesObservados, 1), 50)
    : 1;

  const queryDetalle = useMemo(() => {
    const params = new URLSearchParams();
    params.set('fechaEntrada', filtroActivo.fechaEntrada);
    params.set('fechaSalida', filtroActivo.fechaSalida);
    params.set('totalOcupantes', String(filtroActivo.totalOcupantes));
    for (const fecha of filtroActivo.fechasNacimiento ?? []) {
      params.append('fechasNacimiento', fecha);
    }
    return params.toString();
  }, [filtroActivo]);

  return (
    <section>
      <section className="hero" aria-label="Presentación del alojamiento">
        <div className="hero-copy">
          <Etiqueta tono="light">
            <Icono nombre="mapa" tamano={14} /> {alojamiento?.ciudad ?? 'Armenia, Quindío'}
          </Etiqueta>
          <h1>
            {alojamiento ? (
              alojamiento.nombre
            ) : (
              <>
                Siete Raíces – <em>Apartamentos Turísticos</em>
              </>
            )}
          </h1>
          <p>
            {alojamiento
              ? alojamiento.descripcion
              : 'Consultando información oficial del alojamiento en el servidor…'}
          </p>
        </div>

        <div className="hero-facts" aria-label="Datos operativos del alojamiento">
          <div>
            <b>{alojamiento ? hora(alojamiento.horaEntrada) : '—'}</b>
            <span>Check-in</span>
          </div>
          <i />
          <div>
            <b>{alojamiento ? hora(alojamiento.horaSalida) : '—'}</b>
            <span>Check-out</span>
          </div>
          <i />
          <div>
            <b>{politica ? `v${politica.version}` : '—'}</b>
            <span>Política vigente</span>
          </div>
        </div>
      </section>

      <form onSubmit={handleSubmit(alBuscar)} noValidate aria-label="Buscar apartamentos disponibles">
        <div className="search-panel">
          <div className="field">
            <label htmlFor="fechaEntrada">Entrada</label>
            <span>
              <Icono nombre="calendario" tamano={16} />
              <input
                id="fechaEntrada"
                type="date"
                min={hoy}
                aria-invalid={errors.fechaEntrada ? true : undefined}
                {...register('fechaEntrada')}
              />
            </span>
            {errors.fechaEntrada && (
              <span className="error" role="alert">
                {errors.fechaEntrada.message}
              </span>
            )}
          </div>

          <div className="field">
            <label htmlFor="fechaSalida">Salida</label>
            <span>
              <Icono nombre="calendario" tamano={16} />
              <input
                id="fechaSalida"
                type="date"
                min={entradaObservada || hoy}
                aria-invalid={errors.fechaSalida ? true : undefined}
                {...register('fechaSalida')}
              />
            </span>
            {errors.fechaSalida && (
              <span className="error" role="alert">
                {errors.fechaSalida.message}
              </span>
            )}
          </div>

          <div className="field">
            <label htmlFor="totalOcupantes">Grupo (ocupantes)</label>
            <span>
              <Icono nombre="usuarios" tamano={16} />
              <input
                id="totalOcupantes"
                type="number"
                min={1}
                max={50}
                step={1}
                aria-invalid={errors.totalOcupantes ? true : undefined}
                {...register('totalOcupantes', {
                  valueAsNumber: true,
                  onChange: (evento) => {
                    if (getValues('incluirEdades')) {
                      sincronizarArregloEdades(Number(evento.target.value));
                    }
                  },
                })}
              />
            </span>
            {errors.totalOcupantes && (
              <span className="error" role="alert">
                {errors.totalOcupantes.message}
              </span>
            )}
          </div>

          <div className="field">
            <label htmlFor="caracteristicaPrincipal">Característica</label>
            <span>
              <Icono nombre="filtro" tamano={16} />
              <select id="caracteristicaPrincipal" {...register('caracteristicaPrincipal')}>
                <option value="">Todas</option>
                {CARACTERISTICAS_OPCIONES.map((caracteristica) => (
                  <option key={caracteristica} value={caracteristica}>
                    {caracteristica}
                  </option>
                ))}
              </select>
            </span>
          </div>

          <button type="submit" className="primary search-button" disabled={cargandoResultados}>
            <Icono nombre="buscar" tamano={16} /> {cargandoResultados ? 'Buscando…' : 'Buscar'}
          </button>
        </div>

        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '10px',
            marginTop: '12px',
          }}
        >
          <button
            type="button"
            className="secondary compact"
            onClick={() => setMostrarFiltrosAvanzados((actual) => !actual)}
            aria-expanded={mostrarFiltrosAvanzados}
          >
            <Icono nombre="ajustes" tamano={14} />{' '}
            {mostrarFiltrosAvanzados ? 'Ocultar filtros adicionales' : 'Filtros adicionales'}
            {conteoFiltrosAvanzados > 0 ? ` (${conteoFiltrosAvanzados})` : ''}
          </button>

          {conteoFiltrosAvanzados > 0 && (
            <button type="button" className="text-action" style={{ paddingTop: 0 }} onClick={restablecerFiltros}>
              Limpiar filtros
            </button>
          )}
        </div>

        {mostrarFiltrosAvanzados && (
          <div className="form-section" style={{ marginTop: '12px' }}>
            <div className="filtros" style={{ border: 0, padding: 0, marginBottom: '12px' }}>
              <label htmlFor="capacidadMinima">
                CAPACIDAD MÍNIMA
                <input
                  id="capacidadMinima"
                  type="number"
                  min={1}
                  max={50}
                  placeholder="Ej. 2"
                  aria-invalid={errors.capacidadMinima ? true : undefined}
                  {...register('capacidadMinima')}
                />
                {errors.capacidadMinima && (
                  <span className="error" role="alert">
                    {errors.capacidadMinima.message}
                  </span>
                )}
              </label>

              <label htmlFor="dormitoriosMinimos">
                DORMITORIOS MÍNIMOS
                <input
                  id="dormitoriosMinimos"
                  type="number"
                  min={1}
                  max={10}
                  placeholder="Ej. 2"
                  aria-invalid={errors.dormitoriosMinimos ? true : undefined}
                  {...register('dormitoriosMinimos')}
                />
                {errors.dormitoriosMinimos && (
                  <span className="error" role="alert">
                    {errors.dormitoriosMinimos.message}
                  </span>
                )}
              </label>

              <label htmlFor="precioMinimo">
                PRECIO MÍNIMO (COP)
                <input
                  id="precioMinimo"
                  type="number"
                  min={0}
                  step={1000}
                  placeholder="Sin mínimo"
                  aria-invalid={errors.precioMinimo ? true : undefined}
                  {...register('precioMinimo')}
                />
                {errors.precioMinimo && (
                  <span className="error" role="alert">
                    {errors.precioMinimo.message}
                  </span>
                )}
              </label>

              <label htmlFor="precioMaximo">
                PRECIO MÁXIMO (COP)
                <input
                  id="precioMaximo"
                  type="number"
                  min={0}
                  step={1000}
                  placeholder="Sin máximo"
                  aria-invalid={errors.precioMaximo ? true : undefined}
                  {...register('precioMaximo')}
                />
                {errors.precioMaximo && (
                  <span className="error" role="alert">
                    {errors.precioMaximo.message}
                  </span>
                )}
              </label>

              <div className="button-row">
                <button type="button" className="secondary compact" onClick={restablecerFiltros}>
                  Limpiar
                </button>
                <button type="submit" className="primary compact" disabled={cargandoResultados}>
                  <Icono nombre="filtro" tamano={14} /> Aplicar
                </button>
              </div>
            </div>

            <div style={{ borderTop: '1px solid var(--line)', paddingTop: '12px', marginTop: '4px' }}>
              <span className="eyebrow" style={{ marginBottom: '8px' }}>
                CARACTERÍSTICAS ADICIONALES
              </span>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                {CARACTERISTICAS_OPCIONES.map((caracteristica) => {
                  const seleccionada = caracteristicasAdicionalesObservadas.includes(caracteristica);
                  return (
                    <label
                      key={caracteristica}
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '6px',
                        padding: '5px 9px',
                        borderRadius: '7px',
                        border: `1px solid ${seleccionada ? 'var(--green)' : 'var(--line)'}`,
                        background: seleccionada ? 'var(--sage)' : '#fff',
                        fontSize: '10px',
                        fontWeight: 600,
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={seleccionada}
                        onChange={() => alternarCaracteristicaAdicional(caracteristica)}
                      />
                      {caracteristica}
                    </label>
                  );
                })}
              </div>
              {errors.caracteristicasAdicionales && (
                <span className="error" role="alert">
                  {errors.caracteristicasAdicionales.message}
                </span>
              )}
            </div>

            <div style={{ borderTop: '1px solid var(--line)', paddingTop: '12px', marginTop: '12px' }}>
              <label
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '8px',
                  fontSize: '11px',
                  fontWeight: 700,
                  color: 'var(--ink)',
                }}
              >
                <input
                  type="checkbox"
                  {...register('incluirEdades', {
                    onChange: (evento) => {
                      if (evento.target.checked) {
                        sincronizarArregloEdades(Number(getValues('totalOcupantes')));
                      }
                    },
                  })}
                />
                Indicar fechas de nacimiento del grupo (opcional: menores de 10 años sin cargo)
              </label>

              {incluirEdadesObservado && (
                <div className="form-grid" style={{ marginTop: '10px' }}>
                  {Array.from({ length: cantidadOcupantesEdades }, (_, indice) => {
                    const errorFecha = errors.fechasNacimiento?.[indice]?.message;
                    return (
                      <label key={indice} htmlFor={`nacimiento-busqueda-${indice}`}>
                        OCUPANTE {indice + 1} · FECHA DE NACIMIENTO
                        <input
                          id={`nacimiento-busqueda-${indice}`}
                          type="date"
                          max={ayer}
                          aria-invalid={errorFecha ? true : undefined}
                          {...register(`fechasNacimiento.${indice}`)}
                        />
                        {errorFecha && (
                          <span className="error" role="alert">
                            {errorFecha}
                          </span>
                        )}
                      </label>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}
      </form>

      {errorAlojamiento && (
        <p className="form-error bloque" role="alert" style={{ marginTop: '16px' }}>
          <Icono nombre="alerta" tamano={16} /> No fue posible cargar la información del alojamiento: {errorAlojamiento}
        </p>
      )}

      <div className="section-heading">
        <div>
          <span className="eyebrow">
            {nochesConsulta > 0
              ? `ESTANCIA DE ${nochesConsulta} ${nochesConsulta === 1 ? 'NOCHE' : 'NOCHES'}`
              : 'DISPONIBILIDAD'}
          </span>
          <h2>Apartamentos disponibles</h2>
          <p>
            {cargandoResultados
              ? 'Consultando disponibilidad y tarifas en el servidor…'
              : pagina
                ? `${pagina.totalElementos} ${pagina.totalElementos === 1 ? 'opción' : 'opciones'} para ${filtroActivo.totalOcupantes} ${
                    filtroActivo.totalOcupantes === 1 ? 'persona' : 'personas'
                  } · valor estimado con tarifa de temporada`
                : 'Consulte las fechas para ver tarifas estimadas.'}
          </p>
        </div>

        <button
          type="button"
          className="secondary"
          onClick={() => setMostrarInfo((actual) => !actual)}
          aria-expanded={mostrarInfo}
        >
          <Icono nombre="mapa" tamano={16} />{' '}
          {mostrarInfo ? 'Ocultar normas y política' : 'Normas, servicios y política'}
        </button>
      </div>

      {mostrarInfo && (
        <section
          className="form-section"
          aria-label="Normas de convivencia, servicios adicionales y política de cancelación"
          style={{ marginBottom: '20px' }}
        >
          {!alojamiento && !politica && !errorAlojamiento && <Cargando filas={2} />}

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
              gap: '18px',
            }}
          >
            {alojamiento && (
              <div>
                <span className="eyebrow">UBICACIÓN Y HORARIOS</span>
                <h3 style={{ font: '750 15px "Manrope"', margin: '4px 0 8px' }}>{alojamiento.nombre}</h3>
                <p style={{ fontSize: '11px', color: 'var(--muted)', lineHeight: 1.5 }}>
                  {alojamiento.direccion} · {alojamiento.ciudad}
                </p>
                <p style={{ fontSize: '11px', color: 'var(--ink)', marginTop: '8px' }}>
                  <strong>Entrada:</strong> {hora(alojamiento.horaEntrada)} · <strong>Salida:</strong>{' '}
                  {hora(alojamiento.horaSalida)}
                </p>

                <span className="eyebrow" style={{ marginTop: '14px' }}>
                  SERVICIOS ADICIONALES
                </span>
                <ul
                  style={{
                    margin: '6px 0 0',
                    paddingLeft: '16px',
                    fontSize: '11px',
                    color: 'var(--muted)',
                    lineHeight: 1.6,
                  }}
                >
                  {alojamiento.servicios.map((servicio) => (
                    <li key={servicio.codigo}>
                      <strong style={{ color: 'var(--ink)' }}>{servicio.nombre}:</strong>{' '}
                      {servicio.generaCargo ? pesos(servicio.valor) : 'Sin cargo'} (
                      {nombreUnidadCobro[servicio.unidad].toLowerCase()})
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {alojamiento && (
              <div>
                <span className="eyebrow">NORMAS DE CONVIVENCIA</span>
                <ul
                  style={{
                    margin: '8px 0 0',
                    paddingLeft: '16px',
                    fontSize: '11px',
                    color: 'var(--muted)',
                    lineHeight: 1.6,
                  }}
                >
                  {alojamiento.normas.map((norma, indice) => (
                    <li key={indice}>{norma}</li>
                  ))}
                </ul>
              </div>
            )}

            {politica && (
              <div>
                <span className="eyebrow">POLÍTICA DE CANCELACIÓN · VERSIÓN {politica.version}</span>
                <ul
                  style={{
                    margin: '8px 0 0',
                    paddingLeft: '16px',
                    fontSize: '11px',
                    color: 'var(--muted)',
                    lineHeight: 1.6,
                  }}
                >
                  {politica.tramos.map((tramo) => (
                    <li key={tramo.antelacionMinimaHoras}>
                      <strong style={{ color: 'var(--ink)' }}>{textoAntelacion(tramo.antelacionMinimaHoras)}:</strong>{' '}
                      retención del {tramo.porcentaje}%
                    </li>
                  ))}
                  <li>
                    <strong style={{ color: 'var(--ink)' }}>Inasistencia (No-show):</strong> penalidad del{' '}
                    {politica.porcentajeNoShow}% (hora de entrada de referencia: {hora(politica.horaEntrada)}).
                  </li>
                </ul>
              </div>
            )}
          </div>
        </section>
      )}

      {errorResultados && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {errorResultados}
        </p>
      )}

      {cargandoResultados && (
        <Cargando filas={4} texto="Consultando apartamentos disponibles en Siete Raíces…" />
      )}

      {!cargandoResultados && !errorResultados && pagina && pagina.contenido.length === 0 && (
        <div className="empty">
          <Icono nombre="buscar" tamano={28} />
          <h3>No hay apartamentos disponibles</h3>
          <p style={{ marginTop: '6px' }}>
            Ningún apartamento cumple las fechas, la capacidad o los filtros seleccionados.
          </p>
          <div className="button-row" style={{ justifyContent: 'center', marginTop: '14px' }}>
            <button type="button" className="secondary compact" onClick={restablecerFiltros}>
              Restablecer búsqueda
            </button>
          </div>
        </div>
      )}

      {!cargandoResultados && pagina && pagina.contenido.length > 0 && (
        <>
          <div className="apartment-grid">
            {pagina.contenido.map((apartamento) => (
              <TarjetaApartamento
                key={apartamento.identificacion}
                apartamento={apartamento}
                busquedaQuery={queryDetalle}
              />
            ))}
          </div>

          <nav className="pagination" aria-label="Paginación de apartamentos">
            <button
              type="button"
              onClick={() => cambiarPagina(pagina.pagina - 1)}
              disabled={pagina.pagina === 0 || cargandoResultados}
            >
              Anterior
            </button>
            <span>
              Página {pagina.pagina + 1} de {Math.max(pagina.totalPaginas, 1)} · {pagina.totalElementos}{' '}
              {pagina.totalElementos === 1 ? 'resultado' : 'resultados'} ({pagina.tamanio} por página)
            </span>
            <button
              type="button"
              onClick={() => cambiarPagina(pagina.pagina + 1)}
              disabled={pagina.pagina + 1 >= pagina.totalPaginas || cargandoResultados}
            >
              Siguiente
            </button>
          </nav>
        </>
      )}
    </section>
  );
}
