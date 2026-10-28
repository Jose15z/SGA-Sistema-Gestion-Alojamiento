package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Consultar el detalle de una reserva; el huésped solo ve las suyas. */
@Service
public class ConsultarReservaUseCase {
    private final ReservaRepository reservas;

    public ConsultarReservaUseCase(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    @Transactional(readOnly = true)
    public Reserva ejecutar(CodigoReserva codigo, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        return reserva;
    }
}
