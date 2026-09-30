package co.edu.uniquindio.sga.infrastructure.repository.memory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Adaptador para ejercicios de Guía 06, de un solo hilo y sin durabilidad ni transacciones. */
public final class ReservaRepositoryEnMemoria implements ReservaRepository {
    private final Map<CodigoReserva, Reserva> datos = new LinkedHashMap<>();
    @Override public Optional<Reserva> obtenerPorCodigo(CodigoReserva identidad) {
        return Optional.ofNullable(datos.get(Objects.requireNonNull(identidad)));
    }
    @Override public void guardar(Reserva entidad) {
        Objects.requireNonNull(entidad);
        datos.put(entidad.getCodigo(), entidad);
    }

    @Override public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        Objects.requireNonNull(estado);
        return datos.values().stream().filter(r -> r.getEstado() == estado).toList();
    }
    @Override public List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento) {
        Objects.requireNonNull(apartamento);
        return datos.values().stream().filter(r -> r.getApartamento().equals(apartamento)
                && r.getEstado().retieneDisponibilidad()).toList();
    }
}
