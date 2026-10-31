package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/** Uso de un servicio adicional del catálogo: el valor lo calcula el sistema (L-17). */
public record RegistrarServicioFolioRequest(
        @NotBlank String codigoServicio,
        @NotNull @Min(1) @Max(100) Integer cantidad
) {
}
