package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

/**
 * Código único de una reserva.
 *
 * Ejemplo:
 * RES-2026-00042
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
}