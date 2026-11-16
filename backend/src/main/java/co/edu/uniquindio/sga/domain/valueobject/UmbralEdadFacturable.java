package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Edad mínima desde la cual un ocupante genera cargo.
 */
public record UmbralEdadFacturable(int anios) {

    public UmbralEdadFacturable {

        if (anios < 0 || anios > 30) {
            throw new ReglaDominioException(
                    "El umbral de edad facturable debe estar entre 0 y 30 años"
            );
        }
    }
}