package co.edu.uniquindio.sga.infrastructure.repository.memory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Repositorio de bloqueos para ejecución local de un solo hilo. */
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
}
