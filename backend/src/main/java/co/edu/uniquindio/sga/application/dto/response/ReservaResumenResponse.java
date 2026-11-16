package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Reserva en un listado. */
public record ReservaResumenResponse(
        String codigo,
        String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        EstadoReserva estado,
        CanalOrigen canalOrigen,
        String titularNombre,
        Long valorTotal,
        LocalDateTime fechaCreacion
) {
}
