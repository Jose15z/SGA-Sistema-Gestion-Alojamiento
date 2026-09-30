package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.*;
import java.util.*;

/** Consulta sin mutaciones; el resultado no admite cambios estructurales. */
public final class ConsultarReservasPorEstadoUseCase {
    private final ReservaRepository reservas;
    public ConsultarReservasPorEstadoUseCase(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }
    public List<Reserva> ejecutar(EstadoReserva estado) {
        return List.copyOf(reservas.buscarPorEstado(Objects.requireNonNull(estado, "El estado es obligatorio")));
    }
}
