package co.edu.uniquindio.sga.application.exception;

/**
 * Base de las excepciones de aplicación para recursos inexistentes (404).
 * No representan reglas del negocio: el recurso pedido simplemente no existe.
 */
public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
