package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import java.math.BigDecimal;
import java.time.Duration;

/** Porcentaje aplicable desde una antelación inclusive; evita umbrales quemados en servicios. */
public record TramoCancelacion(Duration antelacionMinima, BigDecimal porcentaje) {
    public TramoCancelacion {
        if (antelacionMinima == null || antelacionMinima.isNegative() || porcentaje == null
                || porcentaje.signum() < 0 || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaDominioException("El tramo requiere antelación no negativa y porcentaje entre 0 y 100");
        }
    }
}
