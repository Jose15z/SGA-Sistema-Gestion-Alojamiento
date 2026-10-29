package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Datos del alojamiento, normas de convivencia y servicios adicionales (7.3). */
public record ActualizarAlojamientoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotNull @Size(max = 1000) String descripcion,
        @NotBlank @Size(max = 80) String ciudad,
        @NotBlank @Size(max = 150) String direccion,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitud,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitud,
        @NotNull @Size(max = 30) List<@NotBlank @Size(max = 300) String> normas,
        @NotNull @Size(min = 1, max = 30) List<@Valid ServicioAdicionalRequest> servicios
) {
}
