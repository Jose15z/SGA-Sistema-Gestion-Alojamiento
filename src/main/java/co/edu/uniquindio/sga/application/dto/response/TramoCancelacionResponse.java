package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;

/** Tramo de la política de cancelación. */
public record TramoCancelacionResponse(
        long antelacionMinimaHoras,
        BigDecimal porcentaje
) {
}
