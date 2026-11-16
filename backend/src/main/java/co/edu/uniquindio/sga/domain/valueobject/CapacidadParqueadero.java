package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Cantidad de vehículos que el alojamiento admite simultáneamente (RP-02).
 *
 * @param cupos número total de cupos de parqueadero
 */
public record CapacidadParqueadero(int cupos) {

    public CapacidadParqueadero {
        if (cupos < 0) {
            throw new ReglaDominioException("La capacidad del parqueadero no puede ser negativa");
        }
    }

    /** Indica si caben los vehículos indicados en una misma noche. */
    public boolean admite(int vehiculos) {
        return vehiculos >= 0 && vehiculos <= cupos;
    }
}
