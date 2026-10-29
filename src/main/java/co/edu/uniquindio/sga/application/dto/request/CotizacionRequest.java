package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/** Solicitud de cotización de una estancia. */
public record CotizacionRequest(
        @NotBlank String identificacionApartamento,
        @NotNull LocalDate fechaEntrada,
        @NotNull LocalDate fechaSalida,
        @NotEmpty List<@Valid OcupanteCotizacionRequest> ocupantes
) {
}
