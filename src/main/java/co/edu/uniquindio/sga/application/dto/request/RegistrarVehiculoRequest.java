package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Vehículo que se asocia a la reserva (RP-02). */
public record RegistrarVehiculoRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9 -]{5,9}$", message = "debe ser una placa válida") String placa
) {
}
