package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;

import java.util.Optional;

/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class AlojamientoRepositoryEnMemoria implements AlojamientoRepository {
    private Alojamiento alojamiento;

    @Override public Optional<Alojamiento> obtener() { return Optional.ofNullable(alojamiento); }
    @Override public void guardar(Alojamiento alojamiento) { this.alojamiento = alojamiento; }
}
