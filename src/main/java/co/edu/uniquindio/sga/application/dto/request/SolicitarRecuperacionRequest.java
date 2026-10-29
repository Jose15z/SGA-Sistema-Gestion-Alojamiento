package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Correo al que se envía el código de recuperación (7.2). */
public record SolicitarRecuperacionRequest(@NotBlank @Email String correo) {
}
