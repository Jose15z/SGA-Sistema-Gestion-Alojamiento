package co.edu.uniquindio.sga.application.exception;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
/** Ausencia del folio necesario para ejecutar una operación. */
public final class FolioNoEncontradoException extends RuntimeException {
    public FolioNoEncontradoException(CodigoReserva codigo) {
        super("No se encontró el folio de la reserva " + codigo.valor());
    }
}
