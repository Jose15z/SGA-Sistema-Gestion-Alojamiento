package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Size;

/** Cierre del folio; la autorización es obligatoria si el saldo no es cero (RN-17). */
public record CerrarFolioRequest(
        @Size(max = 200) String autorizacion
) {
}
