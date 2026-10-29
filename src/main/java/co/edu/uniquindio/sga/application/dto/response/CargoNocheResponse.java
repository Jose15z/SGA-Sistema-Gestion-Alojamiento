package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import java.time.LocalDate;

/** Detalle de una noche de la cotización (RN-05). */
public record CargoNocheResponse(
        LocalDate noche,
        TipoTemporada temporada,
        Long tarifaOcupanteNoche,
        int ocupantesFacturables,
        Long subtotalNoche
) {
}
