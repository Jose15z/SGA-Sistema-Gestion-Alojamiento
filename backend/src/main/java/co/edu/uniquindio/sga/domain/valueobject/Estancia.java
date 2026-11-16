package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Rango de fechas durante el cual un apartamento permanece ocupado.
 *
 * El intervalo es cerrado en la entrada y abierto en la salida:
 * [fechaEntrada, fechaSalida)
 */
public record Estancia(
        LocalDate fechaEntrada,
        LocalDate fechaSalida
) {

    public Estancia {

        if (fechaEntrada == null || fechaSalida == null) {
            throw new ReglaDominioException(
                    "La estancia requiere fecha de entrada y fecha de salida"
            );
        }

        // RN-03
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new ReglaDominioException(
                    "La fecha de salida debe ser posterior a la fecha de entrada"
            );
        }
    }

    /**
     * Cantidad de noches de la estancia.
     *
     * Ejemplo:
     * 10 diciembre -> 12 diciembre = 2 noches.
     */
    public int noches() {
        return (int) ChronoUnit.DAYS.between(
                fechaEntrada,
                fechaSalida
        );
    }

    /**
     * Determina si una fecha corresponde a una noche
     * ocupada dentro de la estancia.
     */
    public boolean incluye(LocalDate noche) {

        if (noche == null) {
            return false;
        }

        return !noche.isBefore(fechaEntrada)
                && noche.isBefore(fechaSalida);
    }

    /**
     * Determina si dos estancias comparten al menos una noche.
     */
    public boolean seSolapaCon(Estancia otra) {

        if (otra == null) {
            return false;
        }

        return this.fechaEntrada.isBefore(otra.fechaSalida)
                && otra.fechaEntrada.isBefore(this.fechaSalida);
    }

    /**
     * Devuelve todas las noches efectivamente ocupadas.
     */
    public List<LocalDate> nochesOcupadas() {
        return fechaEntrada
                .datesUntil(fechaSalida)
                .toList();
    }
}