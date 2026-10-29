package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.seguridad.Rol;
import jakarta.validation.constraints.NotNull;

/** Nuevo rol de un usuario; solo lo asigna un administrador (7.2). */
public record AsignarRolRequest(@NotNull Rol rol) {
}
