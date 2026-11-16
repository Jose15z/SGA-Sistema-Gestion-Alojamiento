package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * Una noche del calendario de ocupación (7.5).
 *
 * @param noche   fecha de la noche
 * @param estado  situación de la noche
 * @param reserva código de la reserva que la ocupa, o null
 */
public record NocheCalendario(LocalDate noche, EstadoNoche estado, CodigoReserva reserva) {

    public NocheCalendario {
        if (noche == null || estado == null) {
            throw new ReglaDominioException("La noche del calendario requiere fecha y estado");
        }
    }
}
