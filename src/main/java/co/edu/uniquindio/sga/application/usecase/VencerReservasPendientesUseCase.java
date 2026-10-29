package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RN-21: vence las reservas PENDIENTE que superaron el plazo de confirmación.
 * Lo dispara {@code VencimientoReservasProgramado}, sin intervención manual.
 *
 * <p>Decisión: un solo {@code @Transactional} para el lote. Todas las cuentas se
 * prevalidan antes de modificar la primera reserva, de modo que una cuenta
 * inválida no deja el lote a medias; si algo falla, se revierte todo y la
 * siguiente ejecución lo reintenta.</p>
 */
@Service
public class VencerReservasPendientesUseCase {
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
    @Transactional
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
