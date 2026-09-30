package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Coordina la llegada y la ocupación del apartamento.
 * La infraestructura debe envolver la ejecución completa en una transacción
 * y proteger los agregados frente a escrituras concurrentes.
 */
public class RegistrarLlegadaUseCase {
    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final EntregaApartamentoService entrega;
    private final Clock reloj;

    public RegistrarLlegadaUseCase(
            ReservaRepository reservas,
            ApartamentoRepository apartamentos,
            EntregaApartamentoService entrega,
            Clock reloj
    ) {
        this.reservas = Objects.requireNonNull(reservas);
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.entrega = Objects.requireNonNull(entrega);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** Registra la llegada usando un único momento de la operación. */
    public void ejecutar(CodigoReserva codigo, String autor) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo)
                .orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Apartamento apartamento = entrega.autorizarEntrega(reserva, ahora.toLocalDate());

        // Primero la reserva valida autor y cronología. El apartamento ya
        // fue autorizado y mantiene sus propias validaciones al ocuparse.
        reserva.registrarLlegada(autor, ahora);
        apartamento.marcarOcupado();
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
    }
}
