package co.edu.uniquindio.sga.domain.repository;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import java.util.List;
/** Consulta todos los bloqueos no levantados del apartamento, incluso futuros. */
public interface BloqueoRepository {
    List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento);
    void guardar(Bloqueo bloqueo);
}
