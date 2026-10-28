package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.Objects;

/**
 * L-04: la identificación de cada apartamento es única y estable. Un apartamento
 * no puede comprobar por sí solo que ningún otro use su identificación.
 */
public final class RegistroApartamentoService {

    private final ApartamentoRepository apartamentos;

    public RegistroApartamentoService(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    public void verificarIdentificacionDisponible(IdentificacionApartamento identificacion) {
        if (identificacion == null) {
            throw new ReglaDominioException("La identificación del apartamento es obligatoria");
        }
        if (apartamentos.existe(identificacion)) {
            throw new ReglaDominioException("Ya existe un apartamento con la identificación " + identificacion.valor());
        }
    }
}
