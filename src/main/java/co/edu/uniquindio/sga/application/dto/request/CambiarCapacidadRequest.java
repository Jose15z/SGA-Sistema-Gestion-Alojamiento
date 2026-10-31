package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Nueva capacidad del apartamento. */
public record CambiarCapacidadRequest(
        @NotNull @Min(1) Integer capacidad
) {
}
