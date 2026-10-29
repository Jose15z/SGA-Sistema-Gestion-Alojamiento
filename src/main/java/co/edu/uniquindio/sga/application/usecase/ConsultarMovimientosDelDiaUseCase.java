package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Llegadas y salidas previstas para un día (7.1, 7.6). */
@Service
public class ConsultarMovimientosDelDiaUseCase {
    private final ReservaRepository reservas;

    public ConsultarMovimientosDelDiaUseCase(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    @Transactional(readOnly = true)
    public Resultado ejecutar(LocalDate dia) {
        Objects.requireNonNull(dia, "El día es obligatorio");
        return new Resultado(dia, reservas.buscarLlegadasPrevistas(dia), reservas.buscarSalidasPrevistas(dia));
    }

    /**
     * @param llegadas reservas pendientes o confirmadas que entran ese día
     * @param salidas  reservas en curso que salen ese día
     */
    public record Resultado(LocalDate dia, List<Reserva> llegadas, List<Reserva> salidas) {
    }
}
