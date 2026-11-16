package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Novedad de llegada informada antes de la hora límite del no-show (L-15). */
public record NovedadLlegadaRequest(
        @NotBlank @Size(max = 300) String novedad
) {
}
