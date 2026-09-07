package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Número máximo de ocupantes permitido en un apartamento.
 */
public record Capacidad(int valor) {

    public Capacidad {
        if (valor <= 0) {
            throw new ReglaDominioException(
                    "La capacidad del apartamento debe ser mayor que cero"
            );
        }
    }

    public boolean admite(int totalOcupantes) {
        return totalOcupantes > 0 && totalOcupantes <= valor;
    }
}