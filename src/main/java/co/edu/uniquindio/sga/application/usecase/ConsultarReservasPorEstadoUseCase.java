package co.edu.uniquindio.sga.application.usecase;

import java.util.List;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

public class ConsultarReservasPorEstadoUseCase {
    private final ReservaRepository reservaRepository;

    public ConsultarReservasPorEstadoUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> ejecutar(EstadoReserva estado) {
        // Simple lectura usando el repositorio
        return reservaRepository.buscarPorEstado(estado);
    }
}
