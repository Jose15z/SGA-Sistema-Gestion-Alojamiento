package co.edu.uniquindio.sga.domain.exception;

/**
 * Excepción utilizada cuando se viola una regla del dominio.
 */
public class ReglaDominioException extends RuntimeException {

    public ReglaDominioException(String mensaje) {
        super(mensaje);
    }
}