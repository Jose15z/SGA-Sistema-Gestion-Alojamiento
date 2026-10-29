package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoNoche;

import java.time.LocalDate;

/** Una noche del calendario de ocupación; la reserva solo se muestra al personal del alojamiento. */
public record NocheCalendarioResponse(
        LocalDate noche,
        EstadoNoche estado,
        String reserva
) {
}
