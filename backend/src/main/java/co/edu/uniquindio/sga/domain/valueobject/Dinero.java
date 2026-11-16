package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valor monetario inmutable en pesos colombianos (COP),
 * expresado sin decimales.
 *
 * <p>Admite valores positivos, cero y negativos. Los negativos
 * permiten representar ajustes y saldos a favor.</p>
 *
 * <p>El constructor normaliza el valor al peso más cercano,
 * utilizando HALF_UP, según el ejemplo de la Guía 04.</p>
 *
 * <p>Los cálculos que produzcan fracciones de peso deben
 * completarse con BigDecimal antes de construir el resultado
 * monetario. No debe construirse un Dinero para redondear
 * cada resultado intermedio ni cada descuento nocturno.</p>
 *
 * <p>La suma, resta y multiplicación por enteros operan
 * sobre pesos enteros y no pierden precisión.</p>
 */
public record Dinero(BigDecimal valor) {

    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    /**
     * Construye un valor monetario en pesos enteros.
     *
     * @param valor importe calculado con precisión exacta
     * @throws ReglaDominioException si el valor es nulo
     */
    public Dinero {
        if (valor == null) {
            throw new ReglaDominioException(
                    "El valor monetario es obligatorio"
            );
        }

        valor = valor.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Construye un importe a partir de pesos enteros.
     *
     * @param pesos cantidad de pesos, que puede ser negativa
     * @return un nuevo valor monetario
     */
    public static Dinero de(long pesos) {
        return new Dinero(BigDecimal.valueOf(pesos));
    }

    /**
     * Suma otro importe sin modificar los valores originales.
     *
     * @param otro importe que se suma
     * @return resultado de la suma
     * @throws ReglaDominioException si el otro importe es nulo
     */
    public Dinero mas(Dinero otro) {
        if (otro == null) {
            throw new ReglaDominioException(
                    "El importe que se desea sumar es obligatorio"
            );
        }

        return new Dinero(valor.add(otro.valor()));
    }

    /**
     * Resta otro importe sin modificar los valores originales.
     *
     * <p>El resultado puede ser negativo, por ejemplo,
     * cuando un folio tiene saldo a favor del huésped.</p>
     *
     * @param otro importe que se resta
     * @return resultado de la resta
     * @throws ReglaDominioException si el otro importe es nulo
     */
    public Dinero menos(Dinero otro) {
        if (otro == null) {
            throw new ReglaDominioException(
                    "El importe que se desea restar es obligatorio"
            );
        }

        return new Dinero(valor.subtract(otro.valor()));
    }

    /**
     * Multiplica el importe por una cantidad entera.
     *
     * <p>Las restricciones sobre cantidades de ocupantes,
     * noches o servicios pertenecen a los conceptos
     * correspondientes, no al valor monetario.</p>
     *
     * @param cantidad factor entero
     * @return resultado exacto de la multiplicación
     */
    public Dinero por(int cantidad) {
        return new Dinero(
                valor.multiply(BigDecimal.valueOf(cantidad))
        );
    }

    /**
     * Indica si el importe es cero.
     *
     * @return true cuando no existe diferencia monetaria
     */
    public boolean esCero() {
        return valor.signum() == 0;
    }

    /**
     * Indica si el importe es negativo.
     *
     * @return true cuando el valor es menor que cero
     */
    public boolean esNegativo() {
        return valor.signum() < 0;
    }
}