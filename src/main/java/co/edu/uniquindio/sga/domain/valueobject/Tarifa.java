package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Tarifa de alojamiento por ocupante facturable y por noche,
 * para un apartamento en una temporada.
 *
 * <p>Es un objeto de valor inmutable. Su igualdad depende
 * del apartamento, la temporada y el importe.</p>
 *
 * <p>RN-05: el importe se multiplica por la cantidad de
 * ocupantes facturables para calcular el subtotal de una noche.</p>
 *
 * <p>Una actualización tarifaria crea otro valor.
 * No modifica las tarifas conservadas en el desglose
 * de las reservas existentes.</p>
 *
 * <p>Esta clase no determina qué temporada corresponde
 * a una fecha ni verifica que el apartamento tenga
 * configuradas todas sus tarifas.</p>
 *
 * @param apartamento identificación del apartamento
 * @param temporada nombre de la temporada a la que aplica
 * @param valorPorOcupanteNoche importe por ocupante facturable y noche
 */
public record Tarifa(
        IdentificacionApartamento apartamento,
        String temporada,
        Dinero valorPorOcupanteNoche
) {

    /**
     * Construye una tarifa con sus datos obligatorios validados.
     *
     * <p>Se rechazan importes negativos, conforme a la
     * validación de tarifas utilizada en CargoNoche.</p>
     *
     * @throws ReglaDominioException si falta el apartamento,
     *         la temporada o el importe, o si este es negativo
     */
    public Tarifa {
        if (apartamento == null) {
            throw new ReglaDominioException(
                    "La tarifa debe indicar el apartamento"
            );
        }

        if (temporada == null || temporada.isBlank()) {
            throw new ReglaDominioException(
                    "La tarifa debe indicar la temporada"
            );
        }

        if (valorPorOcupanteNoche == null) {
            throw new ReglaDominioException(
                    "El valor de la tarifa por ocupante y noche es obligatorio"
            );
        }

        if (valorPorOcupanteNoche.esNegativo()) {
            throw new ReglaDominioException(
                    "La tarifa por ocupante y noche no puede ser negativa"
            );
        }

        temporada = temporada.trim();
    }
}