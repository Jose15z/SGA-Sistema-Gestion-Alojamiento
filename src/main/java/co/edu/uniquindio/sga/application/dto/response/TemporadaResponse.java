package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import java.time.LocalDate;

/** Temporada configurada. */
public record TemporadaResponse(
        String nombre,
        TipoTemporada tipo,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
