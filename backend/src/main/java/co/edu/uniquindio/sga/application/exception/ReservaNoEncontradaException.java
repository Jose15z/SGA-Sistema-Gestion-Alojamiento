package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.Objects;

/** No existe una reserva con el código indicado (404). */
public class ReservaNoEncontradaException extends RecursoNoEncontradoException {

    private final CodigoReserva codigo;

    public ReservaNoEncontradaException(CodigoReserva codigo) {
        super("No se encontró una reserva con el código "
                + Objects.requireNonNull(codigo, "El código de la reserva es obligatorio").valor());
        this.codigo = codigo;
    }

    public CodigoReserva getCodigo() {
        return codigo;
    }
}
