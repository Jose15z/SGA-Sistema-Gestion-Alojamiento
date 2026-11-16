package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Documento que identifica a un ocupante.
 */
public record DocumentoIdentidad(String numero) {

    public DocumentoIdentidad {

        if (numero == null || numero.isBlank()) {
            throw new ReglaDominioException(
                    "El documento de identidad es obligatorio"
            );
        }

        numero = numero.trim();
    }
}