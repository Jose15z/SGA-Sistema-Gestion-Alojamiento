package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Temporada con fechas (ALTA o ESPECIAL), ambas noches incluidas. */
public record RegistrarTemporadaRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull TipoTemporada tipo,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin
) {
}
