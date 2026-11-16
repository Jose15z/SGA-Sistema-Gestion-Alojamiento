package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.Objects;

/** No existe un apartamento con la identificación indicada (404). */
public class ApartamentoNoEncontradoException extends RecursoNoEncontradoException {

    private final IdentificacionApartamento identificacion;

    public ApartamentoNoEncontradoException(IdentificacionApartamento identificacion) {
        super("No se encontró un apartamento con la identificación "
                + Objects.requireNonNull(identificacion, "La identificación del apartamento es obligatoria").valor());
        this.identificacion = identificacion;
    }

    public IdentificacionApartamento getIdentificacion() {
        return identificacion;
    }
}
