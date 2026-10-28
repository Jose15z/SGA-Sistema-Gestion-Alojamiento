package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.Rol;

/** Usuario registrado, sin la contraseña. */
public record UsuarioResponse(
        String correo,
        String nombreCompleto,
        String documento,
        Rol rol
) {
}
