package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo obligatorio de una operación (cancelar, levantar un bloqueo, resolver una novedad). */
public record MotivoRequest(
        @NotBlank @Size(max = 300) String motivo
) {
}
