package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pago (positivo) o devolución (negativo) en pesos, sin decimales (RN-15). */
public record RegistrarPagoRequest(
        @NotNull Long valor,
        @NotNull MedioPago medio,
        @NotBlank @Size(max = 300) String concepto
) {
}
