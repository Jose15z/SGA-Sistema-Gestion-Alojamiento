package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/** Bloqueo administrativo de un apartamento. */
public record BloqueoResponse(
        UUID identificacion,
        String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String motivo,
        boolean vigente,
        String motivoLevantamiento
) {
}
