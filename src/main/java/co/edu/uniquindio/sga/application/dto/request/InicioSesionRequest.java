package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenciales para iniciar sesión (7.2). */
public record InicioSesionRequest(
        @NotBlank @Email String correo,
        @NotBlank String contrasena
) {
}
