package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Novedad sobre un apartamento (7.6); la fecha y el autor los pone el sistema. */
public record RegistrarNovedadApartamentoRequest(
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull GravedadNovedad gravedad
) {
}
