import axios from 'axios';
import { useEffect, useState, type FormEvent } from 'react';
import {
  nombreEstadoNoche,
  type EstadoNoche,
  type NocheCalendario,
} from '../modelos/apartamento';
import { consultarCalendarioApartamento } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { hoyISO } from '../utilidades/fechas';
import { fechaCorta, noches, sumarDias } from '../utilidades/formato';
import { Cargando } from './Cargando';
import { Etiqueta, type TonoEtiqueta } from './Etiqueta';
import { Icono } from './Icono';

const DIAS_VENTANA = 28;
const MAXIMO_NOCHES_CALENDARIO = 366;

const ESTADOS_LEYENDA: EstadoNoche[] = [
  'DISPONIBLE',
  'RESERVADA',
  'BLOQUEADA',
  'EN_PREPARACION',
  'NO_VENDIBLE',
];

const tonoPorEstadoNoche: Record<EstadoNoche, TonoEtiqueta> = {
  DISPONIBLE: 'available',
  RESERVADA: 'confirmed',
  BLOQUEADA: 'warning',
  EN_PREPARACION: 'neutral',
  NO_VENDIBLE: 'danger',
};

const estiloCeldaPorEstado: Record<EstadoNoche, { background: string; border: string }> = {
  DISPONIBLE: { background: '#f4faf6', border: '#c5dfd0' },
  RESERVADA: { background: '#f2f7fa', border: '#c7dce8' },
  BLOQUEADA: { background: '#fdf7ed', border: '#ebd2aa' },
  EN_PREPARACION: { background: '#f7f8f7', border: '#dce2de' },
  NO_VENDIBLE: { background: '#fdf2f1', border: '#ebc6c3' },
};

function diaSemanaCorto(iso: string): string {
  const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number);
  return new Date(anio, mes - 1, dia)
    .toLocaleDateString('es-CO', { weekday: 'short' })
    .replace('.', '');
}

interface CalendarioApartamentoProps {
  identificacion: string;
}

export function CalendarioApartamento({ identificacion }: CalendarioApartamentoProps) {
  const hoy = hoyISO();
  const [rangoActivo, setRangoActivo] = useState(() => ({
    desde: hoy,
    hasta: sumarDias(hoy, DIAS_VENTANA),
  }));
  const [desdeInput, setDesdeInput] = useState(hoy);
  const [hastaInput, setHastaInput] = useState(() => sumarDias(hoy, DIAS_VENTANA));
  const [errorRango, setErrorRango] = useState<string | null>(null);

  const claveConsulta = `${identificacion}|${rangoActivo.desde}|${rangoActivo.hasta}`;
  const [estadoConsulta, setEstadoConsulta] = useState<{
    clave: string;
    noches: NocheCalendario[];
    error: string | null;
  } | null>(null);

  useEffect(() => {
    const controlador = new AbortController();
    let vigente = true;

    consultarCalendarioApartamento(identificacion, rangoActivo.desde, rangoActivo.hasta, controlador.signal)
      .then((lista) => {
        if (!vigente) return;
        setEstadoConsulta({ clave: claveConsulta, noches: lista, error: null });
      })
      .catch((error) => {
        if (axios.isCancel(error) || !vigente) return;
        setEstadoConsulta({ clave: claveConsulta, noches: [], error: mensajeDeError(error) });
      });

    return () => {
      vigente = false;
      controlador.abort();
    };
  }, [identificacion, rangoActivo.desde, rangoActivo.hasta, claveConsulta]);

  const vigente = estadoConsulta?.clave === claveConsulta ? estadoConsulta : null;
  const cargando = vigente === null;
  const nochesLista = vigente?.noches ?? [];
  const errorApi = vigente?.error ?? null;

  function moverVentana(dias: number) {
    const nuevoDesde = sumarDias(rangoActivo.desde, dias);
    const desdeAcotado = nuevoDesde < hoy ? hoy : nuevoDesde;
    const nuevoHasta = sumarDias(desdeAcotado, DIAS_VENTANA);
    setErrorRango(null);
    setDesdeInput(desdeAcotado);
    setHastaInput(nuevoHasta);
    setRangoActivo({ desde: desdeAcotado, hasta: nuevoHasta });
  }

  function consultarHoy() {
    const fin = sumarDias(hoy, DIAS_VENTANA);
    setErrorRango(null);
    setDesdeInput(hoy);
    setHastaInput(fin);
    setRangoActivo({ desde: hoy, hasta: fin });
  }

  function aplicarRango(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!desdeInput || !hastaInput) {
      setErrorRango('Seleccione las fechas desde y hasta del calendario.');
      return;
    }
    if (hastaInput <= desdeInput) {
      setErrorRango('La fecha final del calendario debe ser posterior a la fecha inicial.');
      return;
    }
    if (noches(desdeInput, hastaInput) > MAXIMO_NOCHES_CALENDARIO) {
      setErrorRango(`El rango del calendario no puede superar ${MAXIMO_NOCHES_CALENDARIO} noches.`);
      return;
    }
    setErrorRango(null);
    setRangoActivo({ desde: desdeInput, hasta: hastaInput });
  }

  return (
    <section aria-labelledby="titulo-calendario" style={{ marginTop: '24px' }}>
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '10px',
          marginBottom: '10px',
        }}
      >
        <div>
          <h4 id="titulo-calendario" style={{ margin: 0 }}>
            Calendario de disponibilidad
          </h4>
          <p style={{ fontSize: '10px', color: 'var(--muted)', marginTop: '2px' }}>
            Del {fechaCorta(rangoActivo.desde)} al {fechaCorta(rangoActivo.hasta)} (
            {noches(rangoActivo.desde, rangoActivo.hasta)} noches)
          </p>
        </div>

        <div className="button-row">
          <button
            type="button"
            className="secondary compact"
            onClick={() => moverVentana(-DIAS_VENTANA)}
            disabled={cargando || rangoActivo.desde <= hoy}
          >
            Anterior
          </button>
          <button
            type="button"
            className="secondary compact"
            onClick={consultarHoy}
            disabled={cargando || rangoActivo.desde === hoy}
          >
            Hoy
          </button>
          <button
            type="button"
            className="secondary compact"
            onClick={() => moverVentana(DIAS_VENTANA)}
            disabled={cargando}
          >
            Siguiente
          </button>
        </div>
      </div>

      <form
        onSubmit={aplicarRango}
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'flex-end',
          gap: '10px',
          marginBottom: '12px',
          padding: '10px 12px',
          background: '#fff',
          border: '1px solid var(--line)',
          borderRadius: '10px',
        }}
      >
        <label style={{ display: 'grid', gap: '4px', fontSize: '9px', fontWeight: 800, color: '#74817b' }}>
          DESDE
          <input
            type="date"
            value={desdeInput}
            onChange={(e) => setDesdeInput(e.target.value)}
            style={{
              height: '34px',
              padding: '0 8px',
              border: '1px solid var(--line)',
              borderRadius: '7px',
              fontSize: '10px',
              color: 'var(--ink)',
            }}
          />
        </label>
        <label style={{ display: 'grid', gap: '4px', fontSize: '9px', fontWeight: 800, color: '#74817b' }}>
          HASTA
          <input
            type="date"
            value={hastaInput}
            min={desdeInput || undefined}
            onChange={(e) => setHastaInput(e.target.value)}
            style={{
              height: '34px',
              padding: '0 8px',
              border: '1px solid var(--line)',
              borderRadius: '7px',
              fontSize: '10px',
              color: 'var(--ink)',
            }}
          />
        </label>
        <button type="submit" className="secondary compact" disabled={cargando}>
          <Icono nombre="calendario" tamano={14} /> Consultar rango
        </button>
      </form>

      {errorRango && (
        <p className="form-error" role="alert" style={{ marginBottom: '10px' }}>
          <Icono nombre="alerta" tamano={14} /> {errorRango}
        </p>
      )}

      <div
        role="region"
        aria-label="Leyenda de estados del calendario"
        style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '12px' }}
      >
        {ESTADOS_LEYENDA.map((estado) => (
          <Etiqueta key={estado} tono={tonoPorEstadoNoche[estado]}>
            <i /> {nombreEstadoNoche[estado]}
          </Etiqueta>
        ))}
      </div>

      {cargando && <Cargando filas={2} texto="Consultando calendario del apartamento…" />}

      {!cargando && errorApi && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {errorApi}
        </p>
      )}

      {!cargando && !errorApi && nochesLista.length === 0 && (
        <div className="empty" style={{ padding: '24px' }}>
          <Icono nombre="calendario" tamano={24} />
          <h3>Sin noches en el rango consultado</h3>
          <p>Ajuste las fechas del calendario para visualizar la disponibilidad nocturna.</p>
        </div>
      )}

      {!cargando && !errorApi && nochesLista.length > 0 && (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(112px, 1fr))',
            gap: '8px',
          }}
        >
          {nochesLista.map((item) => {
            const estilo = estiloCeldaPorEstado[item.estado];
            return (
              <div
                key={item.noche}
                aria-label={`${fechaCorta(item.noche)}: ${nombreEstadoNoche[item.estado]}`}
                style={{
                  padding: '9px 10px',
                  borderRadius: '9px',
                  background: estilo.background,
                  border: `1px solid ${estilo.border}`,
                  display: 'grid',
                  gap: '5px',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <strong style={{ fontSize: '11px', color: 'var(--ink)' }}>{fechaCorta(item.noche)}</strong>
                  <small style={{ fontSize: '9px', color: 'var(--muted)', textTransform: 'capitalize' }}>
                    {diaSemanaCorto(item.noche)}
                  </small>
                </div>
                <Etiqueta tono={tonoPorEstadoNoche[item.estado]}>
                  <i /> {nombreEstadoNoche[item.estado]}
                </Etiqueta>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}
