import { useState } from 'react';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { TarjetaApartamento } from '../componentes/TarjetaApartamento';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';

export default function Inicio() {
  const [personas, setPersonas] = useState(2);

  // Valor derivado: se calcula en cada render a partir del estado, no se guarda aparte.
  const queCaben = apartamentosEjemplo.filter((a) => a.capacidad >= personas);

  return (
    <section>
      <EncabezadoPagina
        antetitulo="DISPONIBILIDAD"
        titulo="Apartamentos"
        descripcion="Búsqueda por fechas y grupo. Diseño de referencia: mockups/pantallas/03-buscar-apartamentos.png"
      />

      <label>
        Personas:{' '}
        <input
          type="number"
          min={1}
          value={personas}
          onChange={(e) => setPersonas(Number(e.target.value))}
        />
      </label>

      <p>{queCaben.length} apartamento(s) admiten {personas} persona(s).</p>

      {queCaben.map((apartamento) => (
        <TarjetaApartamento key={apartamento.identificacion} apartamento={apartamento} />
      ))}
    </section>
  );
}
