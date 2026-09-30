package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * Línea inmutable del desglose de alojamiento por noche.
 *
 * <p>Conserva la fecha, el nombre de la temporada aplicada,
 * la tarifa por ocupante facturable y la cantidad
 * de ocupantes facturables.</p>
 *
 * <p>RN-05: el subtotal corresponde a la tarifa por ocupante
 * multiplicada por la cantidad de ocupantes facturables.</p>
 *
 * <p>RN-22: los valores de una noche cotizada no cambian
 * cuando posteriormente se actualiza el tarifario.</p>
 *
 * <p>Esta línea forma parte del desglose de alojamiento.
 * No representa por sí sola un movimiento del folio.</p>
 */
public record CargoNoche(
        LocalDate noche,
        String temporada,
        Dinero tarifaPorOcupante,
        int ocupantesFacturables
) {

    /**
     * Construye una línea válida del desglose.
     *
     * <p>Siguiendo la Guía 05, permite cero ocupantes facturables.
     * La exigencia de un titular facturable se protege al validar
     * la reserva, no en esta línea de cálculo.</p>
     *
     * @param noche fecha de la noche liquidada
     * @param temporada nombre de la temporada aplicada
     * @param tarifaPorOcupante tarifa por ocupante facturable y noche
     * @param ocupantesFacturables cantidad determinada a la entrada
     * @throws ReglaDominioException si falta un dato obligatorio
     *         o alguno de los valores numéricos es negativo
     */
    public CargoNoche {
        if (noche == null) {
            throw new ReglaDominioException(
                    "El cargo de la noche debe indicar la fecha"
            );
        }

        if (temporada == null || temporada.isBlank()) {
            throw new ReglaDominioException(
                    "Toda noche liquidada debe indicar su temporada"
            );
        }

        if (tarifaPorOcupante == null) {
            throw new ReglaDominioException(
                    "La tarifa por ocupante de la noche es obligatoria"
            );
        }

        if (tarifaPorOcupante.esNegativo()) {
            throw new ReglaDominioException(
                    "La tarifa de la noche no puede ser negativa"
            );
        }

        if (ocupantesFacturables < 0) {
            throw new ReglaDominioException(
                    "La cantidad de ocupantes facturables "
                            + "no puede ser negativa"
            );
        }

        temporada = temporada.trim();
    }

    /**
     * Calcula el subtotal de alojamiento de esta noche,
     * antes de descuentos sobre la estancia completa.
     *
     * <p>La multiplicación de una tarifa en pesos enteros
     * por una cantidad entera no produce fracciones de peso.</p>
     *
     * @return tarifa por ocupante multiplicada por
     *         la cantidad de ocupantes facturables
     */
    public Dinero subtotal() {
        return tarifaPorOcupante.por(ocupantesFacturables);
    }
}