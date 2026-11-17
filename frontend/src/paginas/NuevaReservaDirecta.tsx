import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';
import { Link, useNavigate } from 'react-router';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Etiqueta } from '../componentes/Etiqueta';
import { Icono } from '../componentes/Icono';
import { fotoDeApartamento } from '../datos/fotos';
import { esquemaReservaDirecta, type FormularioReservaDirecta } from '../esquemas/reserva';
import type { Apartamento } from '../modelos/apartamento';
import type { Cotizacion } from '../modelos/reserva';
import { cotizarEstancia, listarApartamentos } from '../servicios/apartamentoServicio';
import { crearReserva } from '../servicios/reservaServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';
import { hoyISO } from '../utilidades/fechas';
import { fechaCorta, noches, pesos, sumarDias } from '../utilidades/formato';

const OCUPANTE_VACIO = { documento: '', nombreCompleto: '', fechaNacimiento: '' };

// Reserva por el canal DIRECTO (7.5): la recepción la crea para un titular que puede no tener cuenta.
export default function NuevaReservaDirecta() {
  const navegar = useNavigate();
  const [apartamentos, setApartamentos] = useState<Apartamento[]>([]);
  const [errorCarga, setErrorCarga] = useState<string | null>(null);
  const [resultado, setResultado] = useState<{
    solicitud: string;
    cotizacion: Cotizacion | null;
    error: string | null;
  } | null>(null);

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioReservaDirecta>({
    resolver: zodResolver(esquemaReservaDirecta),
    mode: 'onTouched',
    defaultValues: {
      identificacionApartamento: '',
      fechaEntrada: hoyISO(),
      fechaSalida: sumarDias(hoyISO(), 2),
      horaEstimadaLlegada: '15:00',
      ocupantes: [OCUPANTE_VACIO],
    },
  });
  const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });

  // Los valores que cambian la cotización se observan para recalcularla.
  const [idApartamento, entrada, salida, ocupantes] = useWatch({
    control,
    name: ['identificacionApartamento', 'fechaEntrada', 'fechaSalida', 'ocupantes'],
  });
  const apartamento = apartamentos.find((a) => a.identificacion === idApartamento);
  const nacimientos = ocupantes.map((o) => o.fechaNacimiento);
  const capacidadCompleta = apartamento ? fields.length >= apartamento.capacidad : false;

  useEffect(() => {
    let vigente = true;
    listarApartamentos()
      .then((lista) => vigente && setApartamentos(lista.filter((a) => a.activo)))
      .catch((e) => vigente && setErrorCarga(mensajeDeError(e)));
    return () => {
      vigente = false;
    };
  }, []);

  // Cotización en vivo (RN-05): la calcula el backend con las temporadas y los ocupantes facturables.
  // La solicitud se arma como texto para que el efecto se repita solo cuando cambia algo que la afecta.
  const solicitud =
    idApartamento && entrada && salida && salida > entrada && nacimientos.every(Boolean)
      ? JSON.stringify({
          identificacionApartamento: idApartamento,
          fechaEntrada: entrada,
          fechaSalida: salida,
          ocupantes: nacimientos.map((fechaNacimiento) => ({ fechaNacimiento })),
        })
      : null;

  useEffect(() => {
    if (!solicitud) return;
    let vigente = true;
    // Espera 400 ms a que el usuario termine de escribir antes de consultar.
    const espera = window.setTimeout(async () => {
      try {
        const cotizacion = await cotizarEstancia(JSON.parse(solicitud));
        if (vigente) setResultado({ solicitud, cotizacion, error: null });
      } catch (e) {
        if (vigente) setResultado({ solicitud, cotizacion: null, error: mensajeDeError(e) });
      }
    }, 400);
    return () => {
      vigente = false;
      window.clearTimeout(espera);
    };
  }, [solicitud]);

  // Solo se muestra la respuesta que corresponde a los datos actuales del formulario.
  const vigente = resultado && resultado.solicitud === solicitud ? resultado : null;
  const cotizacion = vigente?.cotizacion ?? null;
  const errorCotizacion = vigente?.error ?? null;
  const cotizando = solicitud !== null && vigente === null;

  async function alEnviar(datos: FormularioReservaDirecta) {
    const [titular, ...acompanantes] = datos.ocupantes;
    try {
      const reserva = await crearReserva({
        identificacionApartamento: datos.identificacionApartamento,
        fechaEntrada: datos.fechaEntrada,
        fechaSalida: datos.fechaSalida,
        canalOrigen: 'DIRECTO',
        identificadorExterno: null,
        titular,
        acompanantes,
        horaEstimadaLlegada: datos.horaEstimadaLlegada || null,
        mascotas: null,
      });
      navegar(`/recepcion/reservas/${reserva.codigo}/folio`, {
        state: { aviso: `Reserva ${reserva.codigo} creada en estado PENDIENTE. Registre el anticipo para confirmarla.` },
      });
    } catch (error) {
      // Los errores 400 del backend nombran "titular.x" y "acompanantes.N.x": en el formulario son "ocupantes.N.x".
      const porCampo = erroresDeCampo(error);
      const campos = Object.entries(porCampo);
      if (campos.length > 0) {
        for (const [campo, mensaje] of campos) {
          const nombre = campo
            .replace(/^titular\./, 'ocupantes.0.')
            .replace(/^acompanantes\.(\d+)\./, (_, n: string) => `ocupantes.${Number(n) + 1}.`);
          setError(nombre as never, { message: mensaje });
        }
        return;
      }
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  const totalNoches = entrada && salida && salida > entrada ? noches(entrada, salida) : 0;
  const facturables = cotizacion?.detalleNoches[0]?.ocupantesFacturables;

  return (
    <section>
      <EncabezadoPagina
        antetitulo="CANAL DE ORIGEN · DIRECTO"
        titulo="Nueva reserva"
        descripcion="El titular puede no tener cuenta. La reserva se crea en estado PENDIENTE."
      >
        <Link className="secondary" to="/recepcion">
          <Icono nombre="cerrar" /> Cancelar
        </Link>
      </EncabezadoPagina>

      {errorCarga && (
        <p className="form-error bloque" role="alert">
          <Icono nombre="alerta" tamano={16} /> {errorCarga}
        </p>
      )}

      <form className="direct-booking" onSubmit={handleSubmit(alEnviar)} noValidate>
        <div className="booking-form-main">
          <section className="form-section" aria-labelledby="paso-1">
            <div className="form-section-title">
              <span>1</span>
              <div>
                <h3 id="paso-1">Estancia y apartamento</h3>
                <p>El backend valida fechas, tarifas, capacidad y disponibilidad.</p>
              </div>
            </div>
            <div className="form-grid">
              <label htmlFor="identificacionApartamento" className="wide">
                Apartamento
                <select
                  id="identificacionApartamento"
                  aria-invalid={errors.identificacionApartamento ? true : undefined}
                  {...register('identificacionApartamento')}
                >
                  <option value="">Seleccione un apartamento</option>
                  {apartamentos.map((a) => (
                    <option key={a.identificacion} value={a.identificacion}>
                      {a.identificacion} · {a.nombre} · hasta {a.capacidad} personas
                      {a.estadoOperativo === 'FUERA_DE_SERVICIO' ? ' · fuera de servicio' : ''}
                    </option>
                  ))}
                </select>
                {errors.identificacionApartamento && (
                  <span className="error" role="alert">
                    {errors.identificacionApartamento.message}
                  </span>
                )}
              </label>
              <label htmlFor="fechaEntrada">
                Fecha de entrada
                <input
                  id="fechaEntrada"
                  type="date"
                  min={hoyISO()}
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
                Fecha de salida
                <input
                  id="fechaSalida"
                  type="date"
                  min={entrada || hoyISO()}
                  aria-invalid={errors.fechaSalida ? true : undefined}
                  {...register('fechaSalida')}
                />
                {errors.fechaSalida && (
                  <span className="error" role="alert">
                    {errors.fechaSalida.message}
                  </span>
                )}
              </label>
              <label htmlFor="horaEstimadaLlegada" className="wide">
                Hora estimada de llegada
                <input id="horaEstimadaLlegada" type="time" {...register('horaEstimadaLlegada')} />
                <small>Opcional al crear; debe estar registrada antes de confirmar la reserva.</small>
              </label>
            </div>
          </section>

          <section className="form-section" aria-labelledby="paso-2">
            <div className="form-section-title">
              <span>2</span>
              <div>
                <h3 id="paso-2">Ocupantes</h3>
                <p>El primer ocupante es el titular. Todos cuentan para la capacidad; los menores de 10 años no se cobran.</p>
              </div>
            </div>

            {fields.map((campo, indice) => {
              const error = errors.ocupantes?.[indice];
              return (
                <fieldset className="occupant-card" key={campo.id}>
                  <legend>
                    {indice === 0 ? 'Titular' : `Ocupante ${indice + 1}`}
                    {indice > 0 && (
                      <button type="button" onClick={() => remove(indice)} aria-label={`Quitar al ocupante ${indice + 1}`}>
                        Quitar
                      </button>
                    )}
                  </legend>
                  <label htmlFor={`documento-${indice}`}>
                    Documento
                    <input
                      id={`documento-${indice}`}
                      placeholder="Ej. 1094000123"
                      aria-invalid={error?.documento ? true : undefined}
                      {...register(`ocupantes.${indice}.documento`)}
                    />
                    {error?.documento && <span className="error">{error.documento.message}</span>}
                  </label>
                  <label htmlFor={`nombre-${indice}`}>
                    Nombre completo
                    <input
                      id={`nombre-${indice}`}
                      placeholder="Nombre y apellidos"
                      aria-invalid={error?.nombreCompleto ? true : undefined}
                      {...register(`ocupantes.${indice}.nombreCompleto`)}
                    />
                    {error?.nombreCompleto && <span className="error">{error.nombreCompleto.message}</span>}
                  </label>
                  <label htmlFor={`nacimiento-${indice}`}>
                    Fecha de nacimiento
                    <input
                      id={`nacimiento-${indice}`}
                      type="date"
                      max={hoyISO()}
                      aria-invalid={error?.fechaNacimiento ? true : undefined}
                      {...register(`ocupantes.${indice}.fechaNacimiento`)}
                    />
                    {error?.fechaNacimiento && <span className="error">{error.fechaNacimiento.message}</span>}
                  </label>
                </fieldset>
              );
            })}

            <button
              type="button"
              className="add-occupant"
              onClick={() => append(OCUPANTE_VACIO)}
              disabled={capacidadCompleta}
            >
              <Icono nombre="mas" />{' '}
              {capacidadCompleta ? `Capacidad completa (${apartamento?.capacidad} personas)` : 'Agregar ocupante'}
            </button>
          </section>
        </div>

        <aside className="booking-review" aria-label="Resumen de la reserva">
          <span className="eyebrow">RESUMEN PRELIMINAR</span>
          <h3>{apartamento ? `${apartamento.identificacion} · ${apartamento.nombre}` : 'Seleccione un apartamento'}</h3>
          <div className="review-image">
            <img src={fotoDeApartamento(apartamento?.identificacion ?? 'APT-301')} alt={apartamento ? `Apartamento ${apartamento.nombre}` : ''} />
            {apartamento && (
              <Etiqueta tono={apartamento.estadoOperativo === 'FUERA_DE_SERVICIO' ? 'danger' : 'available'}>
                <i /> {apartamento.dormitorios} {apartamento.dormitorios === 1 ? 'dormitorio' : 'dormitorios'} · hasta{' '}
                {apartamento.capacidad} personas
              </Etiqueta>
            )}
          </div>
          <div className="review-row">
            <span>Estancia</span>
            <b>{entrada && salida ? `${fechaCorta(entrada)} → ${fechaCorta(salida)}` : '—'}</b>
          </div>
          <div className="review-row">
            <span>Duración</span>
            <b>{totalNoches ? `${totalNoches} ${totalNoches === 1 ? 'noche' : 'noches'}` : '—'}</b>
          </div>
          <div className="review-row">
            <span>Ocupantes</span>
            <b>
              {fields.length} {fields.length === 1 ? 'persona' : 'personas'}
            </b>
          </div>
          <div className="review-row">
            <span>Canal</span>
            <b>DIRECTO</b>
          </div>
          <div className="review-total" aria-live="polite">
            <span>Valor estimado</span>
            <strong>{cotizando ? 'Calculando…' : cotizacion ? pesos(cotizacion.valorTotal) : '—'}</strong>
            <small>
              {cotizacion
                ? `${facturables} facturables × ${cotizacion.totalNoches} noches${cotizacion.valorDescuento ? ` · descuento ${pesos(cotizacion.valorDescuento)}` : ''}`
                : 'Complete apartamento, fechas y fechas de nacimiento.'}
            </small>
          </div>
          {errorCotizacion && (
            <p className="form-error" role="alert">
              <Icono nombre="alerta" tamano={14} /> {errorCotizacion}
            </p>
          )}
          {errors.root?.servidor && (
            <p className="form-error" role="alert">
              <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
            </p>
          )}
          <button type="submit" className="primary full" disabled={isSubmitting}>
            {isSubmitting ? 'Creando reserva…' : 'Crear reserva PENDIENTE'} <Icono nombre="flecha" tamano={18} />
          </button>
          <p className="review-note">
            <Icono nombre="escudo" tamano={14} /> Se abrirá el folio y se congelarán el valor y la política vigente.
          </p>
        </aside>
      </form>
    </section>
  );
}
