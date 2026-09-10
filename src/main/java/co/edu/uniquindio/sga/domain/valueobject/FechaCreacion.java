package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * Fecha calendario en la que fue creada una reserva.
 */
public record FechaCreacion(LocalDate fecha) {

    public FechaCreacion {
        if (fecha == null) {
            throw new ReglaDominioException(
                    "La fecha de creación de la reserva es obligatoria"
            );
        }
    }

    public int dia() {
        return fecha.getDayOfMonth();
    }

    public int mes() {
        return fecha.getMonthValue();
    }

    public int anio() {
        return fecha.getYear();
    }

    public boolean esAnteriorA(LocalDate otraFecha) {
        if (otraFecha == null) {
            throw new ReglaDominioException(
                    "La fecha de comparación es obligatoria"
            );
        }

        return fecha.isBefore(otraFecha);
    }

    public boolean esPosteriorA(LocalDate otraFecha) {
        if (otraFecha == null) {
            throw new ReglaDominioException(
                    "La fecha de comparación es obligatoria"
            );
        }

        return fecha.isAfter(otraFecha);
    }
}