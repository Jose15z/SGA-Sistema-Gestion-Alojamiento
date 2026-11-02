package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** Tramo de la política: desde cuántas horas de antelación aplica el porcentaje. */
public record TramoCancelacionRequest(
        @NotNull @PositiveOrZero Long antelacionMinimaHoras,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal porcentaje
) {
}
