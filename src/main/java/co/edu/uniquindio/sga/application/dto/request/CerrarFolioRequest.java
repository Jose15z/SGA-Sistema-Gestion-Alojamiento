package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Cierre del folio. Si el saldo no es cero, solo un administrador puede cerrarlo
 * y el motivo es obligatorio (7.7, RN-17); el autor sale del token.
 */
public record CerrarFolioRequest(
        @Size(max = 200) String motivo
) {
}
