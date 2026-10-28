package co.edu.uniquindio.sga.application.exception;

/**
 * El solicitante está autenticado pero no puede operar sobre el recurso:
 * por ejemplo, un huésped que intenta ver la reserva de otro (403).
 */
public final class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
