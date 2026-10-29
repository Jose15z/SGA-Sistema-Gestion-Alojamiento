package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Objects;

/**
 * 7.2: no se puede retirar el acceso de una persona que es titular de reservas
 * PENDIENTE, CONFIRMADA o EN_CURSO; sus reservas históricas se conservan.
 *
 * <p>La regla depende de todas las reservas del titular, por eso no vive en un agregado.</p>
 */
public final class TitularReservasService {

    private final ReservaRepository reservas;

    public TitularReservasService(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    public void verificarSinReservasActivas(DocumentoIdentidad titular) {
        if (titular == null) {
            throw new ReglaDominioException("El documento del titular es obligatorio");
        }
        if (reservas.existenActivasDelTitular(titular)) {
            throw new ReglaDominioException("El titular tiene reservas activas: no puede eliminarse");
        }
    }
}
