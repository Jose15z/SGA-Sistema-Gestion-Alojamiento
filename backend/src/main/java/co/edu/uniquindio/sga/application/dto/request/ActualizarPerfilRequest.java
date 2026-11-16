package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos editables del perfil (7.2). El correo no es editable. */
public record ActualizarPerfilRequest(
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Size(max = 30) String telefono
) {
}
