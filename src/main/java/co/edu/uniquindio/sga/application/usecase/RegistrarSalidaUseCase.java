package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

public class RegistrarSalidaUseCase {
    private final ReservaRepository reservaRepository;

    public RegistrarSalidaUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void ejecutar(CodigoReserva codigo) {
        // 1. Obtener la entidad
        Reserva reserva = reservaRepository.obtenerPorCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada")); // Aquí idealmente usar ReservaNoEncontradaException
        
        // 2. Invocar comportamiento del dominio (el cierre de folio lo exige el dominio)
        reserva.registrarSalida();
        
        // 3. Persistir
        reservaRepository.guardar(reserva);
    }
}
