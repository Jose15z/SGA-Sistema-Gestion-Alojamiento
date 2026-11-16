package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/** Integrante del grupo para cotizar: solo importa su fecha de nacimiento (RN-06). */
public record OcupanteCotizacionRequest(
        @NotNull @Past LocalDate fechaNacimiento
) {
}
