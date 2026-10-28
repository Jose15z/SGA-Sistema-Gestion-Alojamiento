package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Alta de un apartamento con sus tarifas (7.3, 7.4). */
public record RegistrarApartamentoRequest(
        @NotBlank @Pattern(regexp = "^APT-[0-9]{3}$", message = "debe tener el formato APT-NNN") String identificacion,
        @NotBlank @Size(max = 80) String nombre,
        @Min(1) int dormitorios,
        @Min(1) int capacidad,
        @NotEmpty List<@Valid TarifaRequest> tarifas
) {
}
