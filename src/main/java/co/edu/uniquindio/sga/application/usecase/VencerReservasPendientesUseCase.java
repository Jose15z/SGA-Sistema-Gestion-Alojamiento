package co.edu.uniquindio.sga.application.usecase;

import java.util.List;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

public class VencerReservasPendientesUseCase {
    private final ReservaRepository reservaRepository;

    public VencerReservasPendientesUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void ejecutar() {
        // Se buscan las reservas en estado PENDIENTE
        List<Reserva> pendientes = reservaRepository.buscarPorEstado(EstadoReserva.PENDIENTE);
        
        for (Reserva reserva : pendientes) {
            // Nota: Aquí se asume que el dominio tiene un método para verificar si expiró
            // o se le pasa la orden de cancelar por vencimiento (RN-21).
            reserva.cancelar("Cancelada automáticamente por vencimiento de plazo");
            reservaRepository.guardar(reserva);
        }
    }
}
