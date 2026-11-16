package co.edu.uniquindio.sga.infrastructure.scheduling;

import co.edu.uniquindio.sga.application.usecase.VencerReservasPendientesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada del tiempo (RN-21): vence las reservas PENDIENTE sin
 * intervención manual. La frecuencia es configuración, no el plazo (L-14).
 */
@Component
public class VencimientoReservasProgramado {

    private static final Logger LOG = LoggerFactory.getLogger(VencimientoReservasProgramado.class);

    private final VencerReservasPendientesUseCase vencerReservasPendientes;

    public VencimientoReservasProgramado(VencerReservasPendientesUseCase vencerReservasPendientes) {
        this.vencerReservasPendientes = vencerReservasPendientes;
    }

    @Scheduled(cron = "${sga.vencimiento.cron}")
    public void ejecutar() {
        int vencidas = vencerReservasPendientes.ejecutar();
        if (vencidas > 0) {
            LOG.info("Reservas PENDIENTE vencidas por superar el plazo de confirmación: {}", vencidas);
        }
    }
}
