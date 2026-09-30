package co.edu.uniquindio.sga.domain.repository;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import java.util.Optional;
/** Las versiones publicadas son inmutables y se conservan aunque cambie la vigente. */
public interface PoliticaCancelacionRepository {
    PoliticaCancelacion obtenerVigente();
    Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version);
}
