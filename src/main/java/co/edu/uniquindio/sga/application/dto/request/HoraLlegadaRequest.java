package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/** Hora estimada de llegada (RN-09). */
public record HoraLlegadaRequest(
        @NotNull LocalTime horaEstimadaLlegada
) {
}
