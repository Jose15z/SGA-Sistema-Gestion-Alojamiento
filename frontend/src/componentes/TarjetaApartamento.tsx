import { Link } from 'react-router';
import { fotoDeApartamento } from '../datos/fotos';
import type { ApartamentoDisponible } from '../modelos/apartamento';
import { pesos } from '../utilidades/formato';
import { Etiqueta } from './Etiqueta';
import { Icono } from './Icono';

interface TarjetaApartamentoProps {
  apartamento: ApartamentoDisponible;
  /** Parámetros de consulta serializados para conservar fechas y ocupantes hacia la vista de detalle. */
  busquedaQuery?: string;
}

export function TarjetaApartamento({ apartamento, busquedaQuery }: TarjetaApartamentoProps) {
  const destino = busquedaQuery
    ? `/apartamentos/${apartamento.identificacion}?${busquedaQuery}`
    : `/apartamentos/${apartamento.identificacion}`;

  return (
    <article className="apartment-card">
      <div className="apartment-image">
        <img
          src={fotoDeApartamento(apartamento.identificacion)}
          alt={`Apartamento ${apartamento.identificacion} ${apartamento.nombre}`}
          loading="lazy"
        />
      </div>

      <div className="apartment-body">
        <div className="apartment-title">
          <div>
            <span>{apartamento.identificacion}</span>
            <h3>{apartamento.nombre}</h3>
          </div>
          <Etiqueta tono="available">
            <i /> Disponible
          </Etiqueta>
        </div>

        <p style={{ fontSize: '11px', color: 'var(--muted)', lineHeight: 1.45, marginTop: '8px' }}>
          {apartamento.descripcion}
        </p>

        <div className="features">
          <span>
            <Icono nombre="cama" tamano={14} /> {apartamento.dormitorios}{' '}
            {apartamento.dormitorios === 1 ? 'dormitorio' : 'dormitorios'}
          </span>
          <span>
            <Icono nombre="usuarios" tamano={14} /> Hasta {apartamento.capacidadMaxima}
          </span>
        </div>

        <div className="tags">
          {apartamento.caracteristicas.map((caracteristica) => (
            <span key={caracteristica}>{caracteristica}</span>
          ))}
        </div>

        <div className="price-row">
          <div>
            <small>
              Total por {apartamento.noches} {apartamento.noches === 1 ? 'noche' : 'noches'}
            </small>
            <strong>{pesos(apartamento.valorEstimado)}</strong>
            <span>Tarifa estimada según temporada</span>
          </div>
          <Link
            className="circle-action"
            to={destino}
            style={{ textDecoration: 'none' }}
            aria-label={`Ver detalle y cotizar ${apartamento.identificacion} ${apartamento.nombre}`}
          >
            <Icono nombre="flecha" tamano={18} />
          </Link>
        </div>

        <Link className="text-action" to={destino} style={{ textDecoration: 'none' }}>
          Ver detalle y cotizar <Icono nombre="flecha" tamano={14} />
        </Link>
      </div>
    </article>
  );
}
