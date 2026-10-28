package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registro público de un huésped (7.2). Solo valida la forma; la unicidad la decide el dominio. */
public record RegistroHuespedRequest(
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Size(min = 8, max = 72) String contrasena,
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Size(max = 30) String documento
) {
}
