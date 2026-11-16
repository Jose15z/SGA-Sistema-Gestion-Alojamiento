package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.application.seguridad.Rol;

import java.time.LocalDate;
import java.util.UUID;

/** Usuario del SGA, sin la contraseña. */
public record UsuarioResponse(
        UUID identificacion,
        String correo,
        String nombreCompleto,
        String telefono,
        String documento,
        LocalDate fechaNacimiento,
        Rol rol,
        boolean activo
) {
}
