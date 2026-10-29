package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;

import java.time.LocalDateTime;

/** Entrada del histórico de tarifas (7.4). */
public record RegistroTarifaResponse(
        TipoTemporada temporada,
        Long valorPorOcupanteNoche,
        LocalDateTime vigenteDesde
) {
}
