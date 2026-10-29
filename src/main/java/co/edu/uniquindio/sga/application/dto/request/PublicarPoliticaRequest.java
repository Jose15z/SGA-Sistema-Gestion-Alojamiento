package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/** Nueva versión de la política de cancelación (L-10). */
public record PublicarPoliticaRequest(
        @NotEmpty List<@Valid TramoCancelacionRequest> tramos,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal porcentajeNoShow,
        @NotNull LocalTime horaEntrada
) {
}
