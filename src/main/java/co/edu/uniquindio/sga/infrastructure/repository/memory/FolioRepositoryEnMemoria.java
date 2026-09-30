package co.edu.uniquindio.sga.infrastructure.repository.memory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Adaptador para ejercicios de Guía 06, de un solo hilo y sin durabilidad ni transacciones. */
public final class FolioRepositoryEnMemoria implements FolioRepository {
    private final Map<CodigoReserva, Folio> datos = new LinkedHashMap<>();
    @Override public Optional<Folio> obtenerPorReserva(CodigoReserva identidad) {
        return Optional.ofNullable(datos.get(Objects.requireNonNull(identidad)));
    }
    @Override public void guardar(Folio entidad) {
        Objects.requireNonNull(entidad);
        datos.put(entidad.getReserva(), entidad);
    }
}
