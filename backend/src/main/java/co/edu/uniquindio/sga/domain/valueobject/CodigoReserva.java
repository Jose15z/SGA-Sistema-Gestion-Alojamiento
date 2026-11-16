package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

/**
 * Código único e inmutable de una reserva.
 *
 * Formato:
 * RES-YYYY-NNNNN
 */
public record CodigoReserva(String valor) {

    private static final Pattern FORMATO =
            Pattern.compile("^RES-\\d{4}-\\d{5}$");

    public CodigoReserva {

        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException(
                    "El código de la reserva es obligatorio"
            );
        }

        valor = valor.trim().toUpperCase();

        if (!FORMATO.matcher(valor).matches()) {
            throw new ReglaDominioException(
                    "El código de la reserva debe tener el formato RES-YYYY-NNNNN"
            );
        }
    }

    public int anio() {
        return Integer.parseInt(valor.substring(4, 8));
    }

    /**
     * Verifica que el año incluido en el código corresponda
     * al año de creación de la reserva.
     */
    public boolean correspondeA(FechaCreacion fechaCreacion) {

        if (fechaCreacion == null) {
            throw new ReglaDominioException(
                    "La fecha de creación es obligatoria"
            );
        }

        return anio() == fechaCreacion.anio();
    }
}