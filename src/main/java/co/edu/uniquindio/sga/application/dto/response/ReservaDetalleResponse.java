package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Detalle completo de una reserva. */
public record ReservaDetalleResponse(
        String codigo,
        String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        CanalOrigen canalOrigen,
        String identificadorExterno,
        EstadoReserva estado,
        LocalDateTime fechaCreacion,
        LocalTime horaEstimadaLlegada,
        String novedadLlegada,
        int mascotas,
        List<String> vehiculos,
        OcupanteResponse titular,
        List<OcupanteResponse> ocupantes,
        CotizacionResponse cotizacionCongelada,
        int versionPolitica,
        List<EventoHistorialResponse> historial
) {
}
