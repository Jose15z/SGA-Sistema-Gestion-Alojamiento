package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Bloqueo de un apartamento entre dos fechas: [fechaEntrada, fechaSalida). */
public record RegistrarBloqueoRequest(
        @NotNull LocalDate fechaEntrada,
        @NotNull LocalDate fechaSalida,
        @NotBlank @Size(max = 300) String motivo
) {
}
