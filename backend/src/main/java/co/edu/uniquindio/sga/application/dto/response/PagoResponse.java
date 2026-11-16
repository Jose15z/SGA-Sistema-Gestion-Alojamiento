package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import java.time.LocalDateTime;
import java.util.UUID;

/** Pago (positivo) o devolución (negativo) registrado en el folio. */
public record PagoResponse(
        UUID identificacion,
        Long valor,
        MedioPago medio,
        LocalDateTime momento,
        String concepto,
        String autor
) {
}
