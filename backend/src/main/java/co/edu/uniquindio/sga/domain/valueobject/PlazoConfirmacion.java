package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Plazo configurable, en horas, para confirmar una reserva pendiente.
 *
 * <p>RN-21: el límite se calcula desde la fecha y hora
 * de creación de la reserva.</p>
 *
 * <p>Es un objeto de valor inmutable. La cantidad de horas
 * procede de la configuración del alojamiento.</p>
 *
 * <p>No consulta el reloj ni modifica el estado de reservas.</p>
 *
 * @param horas duración del plazo, de al menos una hora
 */
public record PlazoConfirmacion(int horas) {

    /**
     * Construye un plazo válido.
     *
     * @param horas duración configurada
     * @throws ReglaDominioException si el plazo es menor
     *         que una hora
     */
    public PlazoConfirmacion {
        if (horas < 1) {
            throw new ReglaDominioException(
                    "El plazo de confirmación debe ser de al menos una hora"
            );
        }
    }

    /**
     * Calcula la fecha y hora límite de confirmación.
     *
     * <p>Conserva la hora de creación y suma las horas
     * configuradas, incluso cuando se cruza un cambio
     * de día, mes o año.</p>
     *
     * <p>Este cálculo no decide si el vencimiento ocurre
     * exactamente en el límite o después de superarlo.</p>
     *
     * @param creacion fecha y hora de creación de la reserva
     * @return momento límite de confirmación
     * @throws ReglaDominioException si la creación es nula
     */
    public LocalDateTime limiteDesde(FechaCreacion creacion) {
        if (creacion == null) {
            throw new ReglaDominioException(
                    "La fecha de creación es obligatoria "
                            + "para calcular el plazo de confirmación"
            );
        }

        return creacion.momento().plusHours(horas);
    }
}