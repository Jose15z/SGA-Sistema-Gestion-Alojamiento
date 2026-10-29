package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Solicitud de reserva. El grupo es el titular más sus acompañantes. Las reglas
 * (capacidad, disponibilidad, mascotas, titular facturable) las decide el dominio (409) */
public record CrearReservaRequest(
        @NotBlank String identificacionApartamento,
        @NotNull LocalDate fechaEntrada,
        @NotNull LocalDate fechaSalida,
        @NotNull CanalOrigen canalOrigen,
        @Size(max = 60) String identificadorExterno,
        @NotNull @Valid OcupanteRequest titular,
        List<@Valid OcupanteRequest> acompanantes,
        LocalTime horaEstimadaLlegada,
        @PositiveOrZero Integer mascotas
) {
}
