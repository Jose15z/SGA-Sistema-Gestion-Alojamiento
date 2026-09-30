package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.Objects;

/**
 * Indica que no existe una reserva con el código solicitado.
 *
 * <p>El repositorio expresa la ausencia mediante Optional.empty().
 * El caso de uso lanza esta excepción cuando necesita
 * una reserva existente para completar la operación.</p>
 *
 * <p>No representa una transición inválida ni otra violación
 * de reglas del dominio.</p>
 */
public class ReservaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final CodigoReserva codigo;

    /**
     * Construye la excepción conservando el código solicitado.
     *
     * @param codigo código de la reserva que no se encontró
     * @throws NullPointerException si se intenta construir
     *         la excepción sin un código
     */
    public ReservaNoEncontradaException(CodigoReserva codigo) {
        super(
                "No se encontró una reserva con el código "
                        + Objects.requireNonNull(
                        codigo,
                        "El código de la reserva es obligatorio"
                ).valor()
        );

        this.codigo = codigo;
    }

    /**
     * Obtiene el código que originó la búsqueda.
     *
     * @return código de la reserva no encontrada
     */
    public CodigoReserva getCodigo() {
        return codigo;
    }
}
