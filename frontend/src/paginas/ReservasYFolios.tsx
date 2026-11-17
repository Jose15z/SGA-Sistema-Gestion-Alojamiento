import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { EtiquetaEstado } from '../componentes/EtiquetaEstado';
import { Icono } from '../componentes/Icono';
import { MovimientosFolio } from '../componentes/MovimientosFolio';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';
import { fotoDeApartamento, nombreDeApartamento } from '../datos/fotos';
import type { Folio } from '../modelos/folio';
import {
  nombreCanal,
  nombreEstado,
  type CanalOrigen,
  type EstadoReserva,
  type FiltroReservas,
  type Pagina,
  type ReservaDetalle,
  type ReservaResumen,
} from '../modelos/reserva';
import { consultarFolio } from '../servicios/folioServicio';
import { consultarReserva, listarReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { fechaCorta, hora, pesos } from '../utilidades/formato';

const ESTADOS: EstadoReserva[] = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_SHOW'];
const CANALES: CanalOrigen[] = ['PORTAL', 'DIRECTO', 'EXTERNO'];
const TAMANO_PAGINA = 6;

// Listado de reservas con filtros (7.5) y el folio de la seleccionada (7.7).
export default function ReservasYFolios() {
  const [filtro, setFiltro] = useState<FiltroReservas>({ page: 0, size: TAMANO_PAGINA });
  const [pagina, setPagina] = useState<Pagina<ReservaResumen> | null>(null);
  const [seleccionada, setSeleccionada] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let vigente = true;
    listarReservas(filtro)
      .then((resultado) => {
        if (!vigente) return;
        setPagina(resultado);
        setError(null);
        // Al cambiar de página o de filtro, se selecciona la primera reserva.
        setSeleccionada(resultado.contenido[0]?.codigo ?? null);
      })
      .catch((e) => vigente && setError(mensajeDeError(e)));
    return () => {
      vigente = false;
    };
  }, [filtro]);

  function aplicarFiltros(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const datos = new FormData(evento.currentTarget);
    const valor = (campo: string) => String(datos.get(campo) ?? '').trim() || undefined;
    setPagina(null);
    setFiltro({
      estado: valor('estado') as EstadoReserva | undefined,
      canal: valor('canal') as CanalOrigen | undefined,
      apartamento: valor('apartamento'),
      titular: valor('titular'),
      page: 0,
      size: TAMANO_PAGINA,
    });
  }

  function irAPagina(numero: number) {
    setPagina(null);
    setFiltro((actual) => ({ ...actual, page: numero }));
  }

  return (
    <section>
      <EncabezadoPagina
        antetitulo="OPERACIÓN FINANCIERA"
        titulo="Reservas y folios"
        descripcion="Consulte movimientos, pagos y saldo de cada reserva sin perder trazabilidad."
      />

      <form className="filtros" onSubmit={aplicarFiltros} onReset={() => setFiltro({ page: 0, size: TAMANO_PAGINA })} aria-label="Filtros">
        <label htmlFor="f-estado">
          ESTADO
          <select id="f-estado" name="estado" defaultValue="">
            <option value="">Todos</option>
            {ESTADOS.map((e) => (
              <option key={e} value={e}>
                {nombreEstado[e]}
              </option>
            ))}
          </select>
        </label>
        <label htmlFor="f-canal">
          CANAL
          <select id="f-canal" name="canal" defaultValue="">
            <option value="">Todos</option>
            {CANALES.map((c) => (
              <option key={c} value={c}>
                {nombreCanal[c]}
              </option>
            ))}
          </select>
        </label>
        <label htmlFor="f-apartamento">
          APARTAMENTO
          <select id="f-apartamento" name="apartamento" defaultValue="">
            <option value="">Todos</option>
            {apartamentosEjemplo.map((a) => (
              <option key={a.identificacion} value={a.identificacion}>
                {a.identificacion} · {a.nombre}
              </option>
            ))}
          </select>
        </label>
        <label htmlFor="f-titular">
          DOCUMENTO DEL TITULAR
          <input id="f-titular" name="titular" maxLength={30} placeholder="Ej. 1094123456" />
        </label>
        <div className="button-row">
          <button type="reset" className="secondary compact">
            Limpiar
          </button>
          <button type="submit" className="primary compact">
            <Icono nombre="filtro" /> Filtrar
          </button>
        </div>
      </form>

      {error && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {error}
        </p>
      )}

      <div className="reservation-layout">
        <div>
          <div className="reservation-list" aria-label="Reservas">
            {!pagina && !error && <Cargando filas={4} />}
            {pagina?.contenido.length === 0 && (
              <div className="empty">
                <Icono nombre="buscar" tamano={26} />
                <h3>No hay reservas con esos filtros</h3>
                <p>Pruebe con otro estado, canal o documento.</p>
              </div>
            )}
            {pagina?.contenido.map((r) => (
              <button
                type="button"
                key={r.codigo}
                className={`reservation-row ${seleccionada === r.codigo ? 'selected' : ''}`}
                onClick={() => setSeleccionada(r.codigo)}
                aria-pressed={seleccionada === r.codigo}
              >
                <img className="res-foto" src={fotoDeApartamento(r.identificacionApartamento)} alt="" />
                <span className="res-main">
                  <span>{r.codigo}</span>
                  <strong>{nombreDeApartamento(r.identificacionApartamento)}</strong>
                  <small>
                    {fechaCorta(r.fechaEntrada)} → {fechaCorta(r.fechaSalida)} · {r.titularNombre} · {r.canalOrigen}
                  </small>
                </span>
                <span className="res-value">
                  <EtiquetaEstado estado={r.estado} />
                  <strong>{pesos(r.valorTotal)}</strong>
                </span>
                <Icono nombre="flecha" tamano={18} />
              </button>
            ))}
          </div>

          {pagina && pagina.totalPaginas > 1 && (
            <nav className="pagination" aria-label="Paginación">
              <button type="button" onClick={() => irAPagina(pagina.pagina - 1)} disabled={pagina.pagina === 0}>
                Anterior
              </button>
              <span>
                Página {pagina.pagina + 1} de {pagina.totalPaginas} · {pagina.totalElementos} reservas
              </span>
              <button
                type="button"
                onClick={() => irAPagina(pagina.pagina + 1)}
                disabled={pagina.pagina + 1 >= pagina.totalPaginas}
              >
                Siguiente
              </button>
            </nav>
          )}
        </div>

        {seleccionada && <PanelFolio key={seleccionada} codigo={seleccionada} />}
      </div>
    </section>
  );
}

// Resumen del folio de la reserva seleccionada; las acciones están en la página del folio.
function PanelFolio({ codigo }: { codigo: string }) {
  const [datos, setDatos] = useState<{ reserva: ReservaDetalle; folio: Folio } | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let vigente = true;
    Promise.all([consultarReserva(codigo), consultarFolio(codigo)])
      .then(([reserva, folio]) => vigente && setDatos({ reserva, folio }))
      .catch((e) => vigente && setError(mensajeDeError(e)));
    return () => {
      vigente = false;
    };
  }, [codigo]);

  return (
    <aside className="folio-card" aria-label={`Folio de ${codigo}`} aria-live="polite">
      {error && (
        <p className="form-error" role="alert">
          <Icono nombre="alerta" tamano={14} /> {error}
        </p>
      )}
      {!datos && !error && <Cargando filas={4} />}
      {datos && (
        <>
          <div className="folio-head">
            <div>
              <span className="eyebrow">FOLIO DE RESERVA</span>
              <h3>{codigo}</h3>
            </div>
            <EtiquetaEstado estado={datos.reserva.estado} />
          </div>
          <div className="folio-summary">
            <div>
              <small>Estancia</small>
              <b>
                {fechaCorta(datos.reserva.fechaEntrada)} → {fechaCorta(datos.reserva.fechaSalida)}
              </b>
            </div>
            <div>
              <small>Apartamento</small>
              <b>{nombreDeApartamento(datos.reserva.identificacionApartamento)}</b>
            </div>
            <div>
              <small>Titular</small>
              <b>{datos.reserva.titular.nombreCompleto}</b>
            </div>
            <div>
              <small>Llegada estimada</small>
              <b>{hora(datos.reserva.horaEstimadaLlegada)}</b>
            </div>
          </div>
          <MovimientosFolio folio={datos.folio} />
          <div className="acciones-folio">
            <Link className="primary full" to={`/recepcion/reservas/${codigo}/folio`}>
              {datos.folio.cerrado ? 'Ver folio completo' : 'Registrar pagos y acciones'} <Icono nombre="flecha" tamano={18} />
            </Link>
          </div>
          <p className="policy-note">
            <Icono nombre="escudo" tamano={14} /> Política de cancelación versión {datos.reserva.versionPolitica}, congelada al
            crear la reserva.
          </p>
        </>
      )}
    </aside>
  );
}
