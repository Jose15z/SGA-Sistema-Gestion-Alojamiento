package co.edu.uniquindio.sga.domain.repository;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import java.util.Optional;
/** Puerto del agregado Folio. Un único folio por reserva; sin eliminación. */
public interface FolioRepository {
    Optional<Folio> obtenerPorReserva(CodigoReserva codigo);
    void guardar(Folio folio);
}
