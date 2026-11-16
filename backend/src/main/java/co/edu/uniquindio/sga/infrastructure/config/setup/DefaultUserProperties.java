package co.edu.uniquindio.sga.infrastructure.config.setup;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import co.edu.uniquindio.sga.application.seguridad.Rol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Datos de demostración configurables; el dominio no depende de los usuarios. */
@Validated
@ConfigurationProperties("sga.usuarios-iniciales")
public record DefaultUserProperties(@NotEmpty List<@Valid UsuarioInicial> usuarios) {
    public record UsuarioInicial(
            @NotNull UUID identificacion,
            @NotBlank @Email @Size(max = 120) String correo,
            @NotBlank @Size(max = 30) String documento,
            @NotNull @Past LocalDate fechaNacimiento,
            @NotBlank @Size(max = 150) String nombreCompleto,
            @NotBlank @Size(max = 30) String telefono,
            @NotNull Rol rol,
            @NotBlank @Pattern(regexp = PoliticaContrasena.PATRON) String contrasena) { }
}
