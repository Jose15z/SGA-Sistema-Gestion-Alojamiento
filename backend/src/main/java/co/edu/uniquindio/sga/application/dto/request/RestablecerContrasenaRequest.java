package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Código de recuperación recibido por correo y la nueva contraseña (7.2). */
public record RestablecerContrasenaRequest(
        @NotBlank @Email String correo,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "debe tener 6 dígitos") String codigo,
        @NotBlank @Pattern(regexp = PoliticaContrasena.PATRON,
                message = "debe tener mínimo 8 caracteres, con mayúsculas y números") String contrasenaNueva
) {
}
