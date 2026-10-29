package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Cambio voluntario de contraseña: la actual y la nueva (7.2). */
public record CambiarContrasenaRequest(
        @NotBlank String contrasenaActual,
        @NotBlank @Pattern(regexp = PoliticaContrasena.PATRON,
                message = "debe tener mínimo 8 caracteres, con mayúsculas y números") String contrasenaNueva
) {
}
