package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

/** El folio de la reserva no existe (404). */
public final class FolioNoEncontradoException extends RecursoNoEncontradoException {
    public FolioNoEncontradoException(CodigoReserva codigo) {
        super("No se encontró el folio de la reserva " + codigo.valor());
    }
}
