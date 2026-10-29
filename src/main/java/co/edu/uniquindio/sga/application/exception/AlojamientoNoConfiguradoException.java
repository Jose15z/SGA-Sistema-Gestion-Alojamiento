package co.edu.uniquindio.sga.application.exception;

/** Aún no se han registrado los datos del alojamiento (404). */
public final class AlojamientoNoConfiguradoException extends RecursoNoEncontradoException {
    public AlojamientoNoConfiguradoException() {
        super("El alojamiento aún no está configurado");
    }
}
