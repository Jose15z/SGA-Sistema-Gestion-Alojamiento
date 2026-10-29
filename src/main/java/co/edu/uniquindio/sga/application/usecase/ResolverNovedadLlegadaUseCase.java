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
import java.util.Objects;

/** Recepción resuelve la novedad de llegada; desde ahí vuelve a poder declararse el no-show. */
@Service
public class ResolverNovedadLlegadaUseCase {
    private final ReservaRepository reservas;
    private final Clock reloj;

    public ResolverNovedadLlegadaUseCase(ReservaRepository reservas, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, String motivo, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        reserva.resolverNovedadLlegada(motivo, solicitante.usuario(), LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        return reserva;
    }
}
