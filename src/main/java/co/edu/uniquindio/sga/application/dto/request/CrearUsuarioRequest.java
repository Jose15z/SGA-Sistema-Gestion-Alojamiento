package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import co.edu.uniquindio.sga.application.seguridad.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Usuario creado por el administrador con un rol interno (7.2). */
public record CrearUsuarioRequest(
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Pattern(regexp = PoliticaContrasena.PATRON,
                message = "debe tener mínimo 8 caracteres, con mayúsculas y números") String contrasena,
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Size(max = 30) String telefono,
        @NotBlank @Size(max = 30) String documento,
        @NotNull @Past LocalDate fechaNacimiento,
        @NotNull Rol rol
) {
}
