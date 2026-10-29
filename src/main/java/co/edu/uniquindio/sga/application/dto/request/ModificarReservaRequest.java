package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Modificación de una reserva no iniciada (7.5). Un campo ausente conserva su valor:
 * se pueden cambiar las fechas (ambas), el apartamento asignado y los acompañantes.
 * El titular no cambia: es quien responde por la reserva.
 */
public record ModificarReservaRequest(
        @Pattern(regexp = "^APT-[0-9]{3}$", message = "debe tener el formato APT-NNN") String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        @Size(max = 30) List<@Valid OcupanteRequest> acompanantes
) {
}
