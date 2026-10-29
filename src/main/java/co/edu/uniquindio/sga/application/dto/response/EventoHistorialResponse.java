package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import java.time.LocalDateTime;

/** Evento del historial de la reserva. */
public record EventoHistorialResponse(
        LocalDateTime momento,
        String accion,
        String autor,
        EstadoReserva estadoAnterior,
        EstadoReserva estadoNuevo,
        String observacion
) {
}
