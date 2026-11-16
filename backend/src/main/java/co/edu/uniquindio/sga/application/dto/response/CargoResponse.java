package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import java.time.LocalDateTime;

/** Cargo del folio. */
public record CargoResponse(
        TipoCargo tipo,
        String concepto,
        Long valor,
        LocalDateTime momento,
        String autor
) {
}
