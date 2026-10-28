package co.edu.uniquindio.sga.application.exception;

/** El correo o la contraseña no corresponden a un usuario activo (401). */
public final class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
