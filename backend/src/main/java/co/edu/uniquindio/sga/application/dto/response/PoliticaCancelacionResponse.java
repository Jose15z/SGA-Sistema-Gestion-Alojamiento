package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/** Versión de la política de cancelación. */
public record PoliticaCancelacionResponse(
        int version,
        List<TramoCancelacionResponse> tramos,
        BigDecimal porcentajeNoShow,
        LocalTime horaEntrada
) {
}
