package co.edu.uniquindio.sga.application.exception;

import java.util.UUID;

/** No existe un bloqueo con la identificación indicada (404). */
public final class BloqueoNoEncontradoException extends RecursoNoEncontradoException {
    public BloqueoNoEncontradoException(UUID identificacion) {
        super("No se encontró el bloqueo " + identificacion);
    }
}
