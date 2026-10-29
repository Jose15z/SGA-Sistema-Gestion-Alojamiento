package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registrar llegada (RN-10, RN-11).
 *
 * <p>Excepción documentada a "un agregado = una transacción" (7.6): la reserva
 * pasa a EN_CURSO y el apartamento a OCUPADO, o no cambia ninguno.</p>
 */
@Service
public class RegistrarLlegadaUseCase {
    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final EntregaApartamentoService entrega;
    private final Clock reloj;

    public RegistrarLlegadaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos,
                                   EntregaApartamentoService entrega, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.entrega = Objects.requireNonNull(entrega);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Solicitante solicitante) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo)
                .orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Apartamento apartamento = entrega.autorizarEntrega(reserva, ahora.toLocalDate());
        // Primero deciden los dos agregados; después se guardan los dos.
        reserva.registrarLlegada(solicitante.usuario(), ahora);
        apartamento.marcarOcupado();
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
        return reserva;
    }
}
