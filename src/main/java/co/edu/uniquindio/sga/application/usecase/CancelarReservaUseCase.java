package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.RetencionCancelacionService;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.*;
import java.util.Objects;

/** Cancela usando la política histórica; reserva y folio se guardan en una transacción de infraestructura. */
public final class CancelarReservaUseCase {
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
    public void ejecutar(CodigoReserva codigo, String motivo, String autor) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        var reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        var folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Dinero penalidad = retencion.calcular(reserva, ahora);
        String concepto = "Cancelación | política " + reserva.getPolitica().numero() + " | " + motivo;
        folio.verificarLiquidacion(penalidad, concepto, autor, ahora);
        reserva.cancelar(motivo, autor, ahora);
        folio.liquidarAlojamiento(penalidad, concepto, autor, ahora);
        reservas.guardar(reserva);
        folios.guardar(folio);
    }
}
