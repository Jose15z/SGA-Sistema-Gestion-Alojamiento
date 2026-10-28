package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Integrante del grupo de la reserva. */
public record OcupanteResponse(
        String documento,
        String nombreCompleto,
        LocalDate fechaNacimiento
) {
}
