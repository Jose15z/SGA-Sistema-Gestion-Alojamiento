package co.edu.uniquindio.sga.application.dto.response;

import java.time.Instant;
import java.util.List;

/** Formato único de error de la API: nunca expone trazas técnicas. */
public record ErrorResponse(
        int estado,
        String codigo,
        String mensaje,
        List<String> detalles,
        Instant marcaTiempo
) {
}
