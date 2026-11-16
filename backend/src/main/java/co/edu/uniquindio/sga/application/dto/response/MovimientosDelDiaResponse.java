package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Llegadas y salidas previstas para un día (7.6). */
public record MovimientosDelDiaResponse(
        LocalDate dia,
        List<ReservaResumenResponse> llegadas,
        List<ReservaResumenResponse> salidas
) {
}
