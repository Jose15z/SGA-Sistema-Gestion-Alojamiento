package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.*;
import java.util.*;

/** Aplica el no-show y su retención histórica; no ocupa ni modifica el apartamento. */
public final class DeclararNoShowUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final RetencionCancelacionService retencion;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;
    public DeclararNoShowUseCase(ReservaRepository reservas, FolioRepository folios,
            RetencionCancelacionService retencion, ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.retencion = Objects.requireNonNull(retencion);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.reloj = Objects.requireNonNull(reloj);
    }
    public void ejecutar(CodigoReserva codigo, String autor) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Dinero penalidad = retencion.calcularNoShow(reserva);
        String concepto = "No-show | política " + reserva.getPolitica().numero();
        folio.verificarLiquidacion(penalidad, concepto, autor, ahora);
        reserva.declararNoShow(configuracion.horaLimiteNoShow(), autor, ahora);
        folio.liquidarAlojamiento(penalidad, concepto, autor, ahora);
        reservas.guardar(reserva);
        folios.guardar(folio);
    }
}
