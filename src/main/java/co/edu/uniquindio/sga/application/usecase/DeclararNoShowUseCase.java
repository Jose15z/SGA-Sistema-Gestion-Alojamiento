package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

public class DeclararNoShowUseCase {
    private final ReservaRepository reservaRepository;

    public DeclararNoShowUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void ejecutar(CodigoReserva codigo) {
        Reserva reserva = reservaRepository.obtenerPorCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
            
        reserva.declararNoShow();
        
        reservaRepository.guardar(reserva);
    }
}
