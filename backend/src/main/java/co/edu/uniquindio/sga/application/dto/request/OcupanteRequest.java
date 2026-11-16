package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Persona del grupo: documento, nombre y fecha de nacimiento (nunca la edad). */
public record OcupanteRequest(
        @NotBlank @Size(max = 30) String documento,
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotNull @Past LocalDate fechaNacimiento
) {
}
