package co.edu.uniquindio.sga.domain.repository;

import java.util.List;
import java.util.Optional;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

public interface ReservaRepository {
    Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo);
    void guardar(Reserva reserva);
    List<Reserva> buscarPorEstado(EstadoReserva estado);
}
