package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import java.util.List;

/** Detalle de un apartamento. */
public record ApartamentoResponse(
        String identificacion,
        String nombre,
        int dormitorios,
        int capacidad,
        EstadoOperativo estadoOperativo,
        boolean activo,
        List<TarifaResponse> tarifas
) {
}
