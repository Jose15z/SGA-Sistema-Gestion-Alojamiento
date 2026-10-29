package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.application.seguridad.Rol;
import java.time.Instant;

/** Token de sesión emitido al iniciar sesión. */
public record TokenResponse(
        String token,
        String tipo,
        Instant expiraEn,
        String correo,
        Rol rol
) {
}
