package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** RN-09: registra la hora estimada de llegada, necesaria para confirmar. */
@Service
public class IndicarHoraLlegadaUseCase {
    private final ReservaRepository reservas;
    private final Clock reloj;

    public IndicarHoraLlegadaUseCase(ReservaRepository reservas, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, LocalTime hora, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        reserva.indicarHoraEstimadaLlegada(hora, solicitante.usuario(), LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        return reserva;
    }
}
