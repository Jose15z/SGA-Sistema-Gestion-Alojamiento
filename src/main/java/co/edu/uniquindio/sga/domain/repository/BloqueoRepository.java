package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto del agregado Bloqueo. */
public interface BloqueoRepository {

    List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento);

    Optional<Bloqueo> obtenerPorIdentificacion(UUID identificacion);

    /** Bloqueos del apartamento, vigentes y levantados, paginados. */
    Pagina<Bloqueo> listarPorApartamento(IdentificacionApartamento apartamento, PeticionPagina pagina);

    void guardar(Bloqueo bloqueo);
}
