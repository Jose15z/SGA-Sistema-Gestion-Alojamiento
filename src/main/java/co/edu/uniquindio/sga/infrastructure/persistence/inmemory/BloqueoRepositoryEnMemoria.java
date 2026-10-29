package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Repositorio de bloqueos para ejecución local de un solo hilo. */
/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class BloqueoRepositoryEnMemoria implements BloqueoRepository {
    private final Map<UUID, Bloqueo> datos = new LinkedHashMap<>();
    @Override public void guardar(Bloqueo bloqueo) {
        Objects.requireNonNull(bloqueo);
        datos.put(bloqueo.getIdentificacion(), bloqueo);
    }
    @Override public List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento) {
        Objects.requireNonNull(apartamento);
        return datos.values().stream().filter(b -> b.estaVigente() && b.getApartamento().equals(apartamento)).toList();
    }
    @Override public Optional<Bloqueo> obtenerPorIdentificacion(UUID identificacion) {
        return Optional.ofNullable(datos.get(identificacion));
    }
    @Override public Pagina<Bloqueo> listarPorApartamento(IdentificacionApartamento apartamento, PeticionPagina pagina) {
        return Pagina.de(datos.values().stream().filter(b -> b.getApartamento().equals(apartamento)).toList(), pagina);
    }
}
