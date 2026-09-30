package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fecha y hora en que se crea una reserva.
 *
 * <p>Es un objeto de valor inmutable. Su igualdad considera
 * tanto la fecha como la hora de creación.</p>
 *
 * <p>RN-21 requiere conservar la hora para evaluar
 * el plazo de confirmación de una reserva pendiente.</p>
 *
 * <p>El momento se recibe desde el exterior del dominio.
 * Esta clase no consulta el reloj del sistema ni asigna
 * automáticamente una hora a una fecha calendario.</p>
 *
 * @param momento fecha y hora de creación de la reserva
 */
public record FechaCreacion(LocalDateTime momento) {

    /**
     * Construye una fecha de creación con precisión temporal.
     *
     * @param momento fecha y hora recibidas por la operación
     * @throws ReglaDominioException si el momento es nulo
     */
    public FechaCreacion {
        if (momento == null) {
            throw new ReglaDominioException(
                    "La fecha y hora de creación de la reserva son obligatorias"
            );
        }
    }

    /**
     * Obtiene la fecha calendario de creación.
     *
     * <p>Esta proyección no debe utilizarse para calcular
     * vencimientos expresados en horas. Para esos cálculos
     * debe utilizarse momento().</p>
     *
     * @return fecha calendario de creación
     */
    public LocalDate fecha() {
        return momento.toLocalDate();
    }

    /**
     * Obtiene el día del mes de creación.
     *
     * @return día del mes
     */
    public int dia() {
        return momento.getDayOfMonth();
    }

    /**
     * Obtiene el mes de creación.
     *
     * @return mes entre uno y doce
     */
    public int mes() {
        return momento.getMonthValue();
    }

    /**
     * Obtiene el año de creación utilizado por el código
     * de la reserva.
     *
     * @return año de creación
     */
    public int anio() {
        return momento.getYear();
    }

    /**
     * Determina si la creación ocurrió estrictamente antes
     * del momento recibido.
     *
     * @param otroMomento fecha y hora de comparación
     * @return true si la creación es anterior
     * @throws ReglaDominioException si la comparación es nula
     */
    public boolean esAnteriorA(LocalDateTime otroMomento) {
        if (otroMomento == null) {
            throw new ReglaDominioException(
                    "La fecha y hora de comparación son obligatorias"
            );
        }

        return momento.isBefore(otroMomento);
    }

    /**
     * Determina si la creación ocurrió estrictamente después
     * del momento recibido.
     *
     * @param otroMomento fecha y hora de comparación
     * @return true si la creación es posterior
     * @throws ReglaDominioException si la comparación es nula
     */
    public boolean esPosteriorA(LocalDateTime otroMomento) {
        if (otroMomento == null) {
            throw new ReglaDominioException(
                    "La fecha y hora de comparación son obligatorias"
            );
        }

        return momento.isAfter(otroMomento);
    }
}