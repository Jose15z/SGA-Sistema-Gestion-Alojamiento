package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Referencia inmutable a una versión de la política de cancelación.
 *
 * <p>RN-13: la retención por cancelación o no-show se determina
 * utilizando la versión asociada a la reserva, no la política
 * vigente al momento de realizar la operación.</p>
 *
 * <p>RN-22: la reserva conserva la versión vigente al crearse.
 * Una actualización posterior de la política del alojamiento
 * no modifica esa referencia.</p>
 *
 * <p>La modificación de una estancia puede recalcular su valor,
 * pero conserva la versión de la política original.</p>
 *
 * <p>Este objeto valida el número de versión. La existencia
 * de dicha versión y la recuperación de sus condiciones
 * se verifican mediante el repositorio correspondiente.</p>
 *
 * @param numero número positivo que identifica la versión
 */
public record VersionPolitica(int numero) {

    /**
     * Construye una referencia válida a una versión de política.
     *
     * @throws ReglaDominioException si el número es menor que uno
     */
    public VersionPolitica {
        if (numero < 1) {
            throw new ReglaDominioException(
                    "La versión de la política debe ser mayor que cero"
            );
        }
    }
}