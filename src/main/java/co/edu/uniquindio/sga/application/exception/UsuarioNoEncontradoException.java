package co.edu.uniquindio.sga.application.exception;

import java.util.UUID;

/** No existe un usuario con la identificación indicada (404). */
public final class UsuarioNoEncontradoException extends RecursoNoEncontradoException {
    public UsuarioNoEncontradoException(UUID identificacion) {
        super("No se encontró el usuario " + identificacion);
    }

    public UsuarioNoEncontradoException(String correo) {
        super("No se encontró el usuario " + correo);
    }
}
