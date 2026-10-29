package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CalendarioTemporadas;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class TemporadaRepositoryEnMemoria implements TemporadaRepository {
    private final List<Temporada> datos = new ArrayList<>();

    @Override public CalendarioTemporadas obtenerCalendario() { return new CalendarioTemporadas(datos); }
    @Override public Pagina<Temporada> listar(PeticionPagina pagina) {
        return Pagina.de(obtenerCalendario().temporadas(), pagina);
    }
    @Override public void guardar(Temporada temporada) { datos.add(Objects.requireNonNull(temporada)); }
}
