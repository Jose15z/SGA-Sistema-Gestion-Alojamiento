package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Tarifa por ocupante facturable y noche para una temporada (COP sin decimales). */
public record TarifaRequest(
        @NotNull TipoTemporada temporada,
        @NotNull @PositiveOrZero Long valorPorOcupanteNoche
) {
}
