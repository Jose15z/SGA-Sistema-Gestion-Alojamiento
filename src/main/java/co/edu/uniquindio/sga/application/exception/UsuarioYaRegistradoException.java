package co.edu.uniquindio.sga.application.exception;

/** El correo o el documento ya pertenecen a otro usuario (409). */
public final class UsuarioYaRegistradoException extends RuntimeException {
    public UsuarioYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
