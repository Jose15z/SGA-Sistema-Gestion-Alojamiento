package co.edu.uniquindio.sga.application.exception;

/** Datos de usuario o de recuperación de contraseña inválidos (400). */
public final class SolicitudUsuarioInvalidaException extends RuntimeException {
    public SolicitudUsuarioInvalidaException(String mensaje) {
        super(mensaje);
    }
}
