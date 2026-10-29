package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;

/**
 * Ubicación exacta del alojamiento (7.3).
 *
 * @param latitud  entre -90 y 90
 * @param longitud entre -180 y 180
 */
public record Ubicacion(BigDecimal latitud, BigDecimal longitud) {

    public Ubicacion {
        if (latitud == null || longitud == null) {
            throw new ReglaDominioException("La ubicación requiere latitud y longitud");
        }
        if (latitud.abs().compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new ReglaDominioException("La latitud debe estar entre -90 y 90");
        }
        if (longitud.abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new ReglaDominioException("La longitud debe estar entre -180 y 180");
        }
    }
}
