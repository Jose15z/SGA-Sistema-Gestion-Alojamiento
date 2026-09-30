package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.*;
import java.util.*;

/** RN-21: vence pendientes después del plazo y revierte alojamiento sin penalidad de cancelación voluntaria. */
public final class VencerReservasPendientesUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;
    public VencerReservasPendientesUseCase(ReservaRepository reservas, FolioRepository folios,
            ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.reloj = Objects.requireNonNull(reloj);
    }
    /** Devuelve cuántas reservas vencieron; una segunda ejecución no repite movimientos. */
    public int ejecutar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        PlazoConfirmacion plazo = Objects.requireNonNull(configuracion.plazoConfirmacion());
        List<Reserva> vencidas = reservas.buscarPorEstado(EstadoReserva.PENDIENTE).stream()
                .filter(r -> r.estaVencida(plazo, ahora)).toList();
        // Prevalida todas las cuentas antes de modificar la primera reserva del lote.
        List<Folio> cuentas = new ArrayList<>();
        for (Reserva reserva : vencidas) {
            Folio folio = folios.obtenerPorReserva(reserva.getCodigo())
                    .orElseThrow(() -> new FolioNoEncontradoException(reserva.getCodigo()));
            folio.verificarLiquidacion(Dinero.CERO, "Vencimiento del plazo de confirmación", "SISTEMA", ahora);
            cuentas.add(folio);
        }
        for (int i = 0; i < vencidas.size(); i++) {
            Reserva reserva = vencidas.get(i);
            Folio folio = cuentas.get(i);
            reserva.vencer(plazo, ahora);
            folio.liquidarAlojamiento(Dinero.CERO, "Vencimiento del plazo de confirmación", "SISTEMA", ahora);
            reservas.guardar(reserva);
            folios.guardar(folio);
        }
        return vencidas.size();
    }
}
