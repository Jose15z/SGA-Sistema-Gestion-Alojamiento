package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;

/** Tarifa por ocupante facturable y noche. */
public record TarifaResponse(
        TipoTemporada temporada,
        Long valorPorOcupanteNoche
) {
}
