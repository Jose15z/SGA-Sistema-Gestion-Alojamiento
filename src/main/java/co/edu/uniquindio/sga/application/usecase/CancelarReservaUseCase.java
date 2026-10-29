package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.RetencionCancelacionService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cancelar reserva (RN-08, RN-12, RN-13).
 *
 * <p>Excepción documentada a "un agregado = una transacción": la retención se
 * liquida en el folio en el mismo instante en que la reserva se cancela.</p>
 */
@Service
public class CancelarReservaUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final RetencionCancelacionService retencion;
    private final Clock reloj;

    public CancelarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                  RetencionCancelacionService retencion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.retencion = Objects.requireNonNull(retencion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, String motivo, Solicitante solicitante) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        String autor = solicitante.usuario();
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Dinero penalidad = retencion.calcular(reserva, ahora);
        String concepto = "Cancelación | política " + reserva.getPolitica().numero() + " | " + motivo;
        folio.verificarLiquidacion(penalidad, concepto, autor, ahora);
        reserva.cancelar(motivo, autor, ahora);
        folio.liquidarAlojamiento(penalidad, concepto, autor, ahora);
        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }
}
