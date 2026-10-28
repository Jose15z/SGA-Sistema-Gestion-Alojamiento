package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Nuevas fechas de la estancia (RN-14). */
public record ModificarEstanciaRequest(
        @NotNull LocalDate fechaEntrada,
        @NotNull LocalDate fechaSalida
) {
}
