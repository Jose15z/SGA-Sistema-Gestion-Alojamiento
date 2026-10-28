package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cargo por servicio adicional (L-17). */
public record RegistrarCargoRequest(
        @NotBlank @Size(max = 300) String concepto,
        @NotNull @Positive Long valor
) {
}
