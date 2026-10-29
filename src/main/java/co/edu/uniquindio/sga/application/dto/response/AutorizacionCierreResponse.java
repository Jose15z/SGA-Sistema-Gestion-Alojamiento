package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;

/** Autorización registrada para cerrar un folio con saldo distinto de cero (7.7). */
public record AutorizacionCierreResponse(
        String autor,
        String motivo,
        LocalDateTime momento
) {
}
