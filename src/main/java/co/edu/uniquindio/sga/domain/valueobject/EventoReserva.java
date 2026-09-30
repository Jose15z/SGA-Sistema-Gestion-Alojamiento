package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Registro inmutable de una acción realizada sobre una reserva.
 *
 * <p>Conserva el momento, la acción, el autor, el estado anterior,
 * el estado resultante y una observación opcional.</p>
 *
 * <p>El momento se recibe desde quien ejecuta la operación.
 * Este objeto no consulta el reloj del sistema.</p>
 *
 * <p>El autor es una referencia textual para trazabilidad.
 * Este objeto no verifica permisos ni conoce credenciales.</p>
 *
 * <p>La reserva controla la incorporación de eventos,
 * su orden cronológico y la protección del historial.</p>
 *
 * @param momento fecha y hora de la acción
 * @param accion nombre de la acción realizada
 * @param autor identificación textual de quien realizó la acción
 * @param estadoAnterior estado previo; null para la creación
 * @param estadoNuevo estado de la reserva después de la acción
 * @param observacion información adicional; puede ser null
 */
public record EventoReserva(
        LocalDateTime momento,
        String accion,
        String autor,
        EstadoReserva estadoAnterior,
        EstadoReserva estadoNuevo,
        String observacion
) {

    /**
     * Construye un evento con sus datos obligatorios validados.
     *
     * <p>El estado anterior y el nuevo pueden coincidir cuando
     * la acción no implica una transición, como indicar
     * la hora estimada de llegada.</p>
     *
     * <p>La legalidad de las transiciones corresponde a Reserva.
     * Este objeto únicamente conserva lo ocurrido.</p>
     *
     * @throws ReglaDominioException si falta el momento,
     *         la acción, el autor o el estado resultante
     */
    public EventoReserva {
        if (momento == null) {
            throw new ReglaDominioException(
                    "El evento debe tener fecha y hora"
            );
        }

        if (accion == null || accion.isBlank()) {
            throw new ReglaDominioException(
                    "El evento debe indicar la acción realizada"
            );
        }

        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException(
                    "El evento debe indicar quién ejecutó la acción"
            );
        }

        if (estadoNuevo == null) {
            throw new ReglaDominioException(
                    "El evento debe indicar el estado resultante de la reserva"
            );
        }

        accion = accion.trim();
        autor = autor.trim();

        if (observacion != null) {
            observacion = observacion.trim();
        }
    }
}