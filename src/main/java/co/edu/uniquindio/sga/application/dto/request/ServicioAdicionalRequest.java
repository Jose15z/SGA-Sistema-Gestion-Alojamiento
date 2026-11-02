package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.UnidadCobro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Servicio adicional del catálogo: si genera cargo, su valor por unidad (7.3, L-17). */
public record ServicioAdicionalRequest(
        @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,29}$", message = "debe usar mayúsculas, números o _") String codigo,
        @NotBlank @Size(max = 80) String nombre,
        @NotNull Boolean generaCargo,
        @NotNull @PositiveOrZero Long valor,
        @NotNull UnidadCobro unidad
) {
}
