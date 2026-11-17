import { useEffect, useRef, useState } from 'react';
import { Cargando } from '../componentes/Cargando';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta, type TonoEtiqueta } from '../componentes/Etiqueta';
import { nombreEstadoOperativo, type Apartamento, type EstadoOperativo } from '../modelos/apartamento';
import { cambiarPreparacion, listarApartamentos } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';

const tonoPorEstado: Record<EstadoOperativo, TonoEtiqueta> = {
  PREPARADO: 'available',
  PENDIENTE_PREPARACION: 'warning',
  EN_PREPARACION: 'confirmed',
  OCUPADO: 'neutral',
  FUERA_DE_SERVICIO: 'danger',
};

export default function Preparacion() {
  const [apartamentos, setApartamentos] = useState<Apartamento[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState('');
  const [ocupado, setOcupado] = useState<string | null>(null);
  const [recarga, setRecarga] = useState(0);
  const mutacionEnCurso = useRef(false);

  useEffect(() => {
    const controlador = new AbortController();
    listarApartamentos(controlador.signal)
      .then((datos) => {
        if (!controlador.signal.aborted) setApartamentos(datos);
      })
      .catch((e: unknown) => {
        if (!controlador.signal.aborted) setError(mensajeDeError(e));
      });
    return () => controlador.abort();
  }, [recarga]);

  async function ejecutar(apartamento: Apartamento) {
    if (mutacionEnCurso.current) return;
    mutacionEnCurso.current = true;
    setOcupado(apartamento.identificacion);
    setError(null);
    setAviso('');
    try {
      const actualizado = await cambiarPreparacion(
        apartamento.identificacion,
        apartamento.estadoOperativo === 'PENDIENTE_PREPARACION' ? 'iniciar-preparacion' : 'marcar-preparado',
      );
      setApartamentos((datos) => datos?.map((actual) =>
        actual.identificacion === actualizado.identificacion ? actualizado : actual,
      ) ?? null);
      setAviso(`${actualizado.nombre}: ${nombreEstadoOperativo[actualizado.estadoOperativo]}.`);
    } catch (e) {
      setApartamentos(null);
      setError(mensajeDeError(e));
      setRecarga((actual) => actual + 1);
    } finally {
      mutacionEnCurso.current = false;
      setOcupado(null);
    }
  }

  return (
    <section>
      <EncabezadoPagina
        antetitulo="OPERACIÓN"
        titulo="Preparación de apartamentos"
        descripcion="Consulta el estado de los apartamentos y registra el inicio o el fin de la preparación."
      />
      {error && (
        <div className="form-error bloque" role="alert">
          <p>{error}</p>
          <button type="button" className="secondary compact" disabled={ocupado !== null} onClick={() => {
            setError(null);
            setRecarga((actual) => actual + 1);
          }}>
            Actualizar apartamentos
          </button>
        </div>
      )}
      {aviso && <p role="status">{aviso}</p>}
      {!apartamentos && !error && <Cargando filas={3} texto="Cargando apartamentos…" />}
      {apartamentos?.length === 0 && <p className="empty">No hay apartamentos activos.</p>}
      {!!apartamentos?.length && (
        <div className="table-card">
          <ul className="preparacion-lista" aria-label="Estado de los apartamentos">
            {apartamentos.map((apartamento) => {
              const puedeIniciar = apartamento.estadoOperativo === 'PENDIENTE_PREPARACION';
              const puedeTerminar = apartamento.estadoOperativo === 'EN_PREPARACION';
              const accion = puedeIniciar ? 'Iniciar preparación' : 'Marcar preparado';
              return (
                <li key={apartamento.identificacion}>
                  <div>
                    <strong>{apartamento.nombre}</strong>
                    <small>{apartamento.identificacion}</small>
                  </div>
                  <Etiqueta tono={tonoPorEstado[apartamento.estadoOperativo]}>
                    {nombreEstadoOperativo[apartamento.estadoOperativo]}
                  </Etiqueta>
                  {(puedeIniciar || puedeTerminar) && (
                    <button type="button" className="secondary compact" disabled={ocupado !== null || error !== null}
                      aria-label={`${accion}: ${apartamento.nombre}`} onClick={() => void ejecutar(apartamento)}>
                      {ocupado === apartamento.identificacion ? 'Guardando…' : accion}
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </section>
  );
}
