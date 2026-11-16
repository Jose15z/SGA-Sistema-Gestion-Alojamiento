package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/**
 * Resultado del cambio de capacidad (7.3): las reservas ya creadas no se tocan;
 * si alguna queda por encima de la nueva capacidad, se advierte y se listan.
 */
public record CambioCapacidadResponse(
        ApartamentoResponse apartamento,
        String advertencia,
        List<ReservaResumenResponse> reservasExcedidas
) {
}
