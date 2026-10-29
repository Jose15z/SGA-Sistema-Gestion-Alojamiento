package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Datos descriptivos editables de un apartamento (7.3). La capacidad y las tarifas tienen su propio endpoint. */
public record ActualizarApartamentoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull @Size(max = 1000) String descripcion,
        @Min(1) int dormitorios,
        @NotNull @Size(max = 20) List<@NotBlank @Size(max = 80) String> caracteristicas
) {
}
