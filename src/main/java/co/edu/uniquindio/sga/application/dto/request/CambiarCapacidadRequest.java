package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;

/** Nueva capacidad del apartamento. */
public record CambiarCapacidadRequest(
        @Min(1) int capacidad
) {
}
