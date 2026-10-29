package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;

import java.time.LocalDateTime;

/** Novedad registrada sobre un apartamento (7.6). */
public record NovedadApartamentoResponse(
        LocalDateTime fecha,
        String autor,
        String descripcion,
        GravedadNovedad gravedad
) {
}
