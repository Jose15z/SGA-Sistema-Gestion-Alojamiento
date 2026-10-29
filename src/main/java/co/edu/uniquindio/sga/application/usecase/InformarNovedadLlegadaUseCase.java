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

/** L-15: una novedad de llegada informada impide declarar el no-show hasta resolverla. */
@Service
public class InformarNovedadLlegadaUseCase {
    private final ReservaRepository reservas;
    private final Clock reloj;

    public InformarNovedadLlegadaUseCase(ReservaRepository reservas, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, String novedad, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        reserva.informarNovedadLlegada(novedad, solicitante.usuario(), LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        return reserva;
    }
}
