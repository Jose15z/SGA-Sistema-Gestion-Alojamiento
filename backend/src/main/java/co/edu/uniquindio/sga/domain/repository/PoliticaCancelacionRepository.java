package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.util.Optional;

/**
 * Puerto del agregado PoliticaCancelacion. Las versiones publicadas
 * no se sobrescriben: las reservas conservan la suya (RN-13, RN-22).
 */
public interface PoliticaCancelacionRepository {

    PoliticaCancelacion obtenerVigente();

    Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version);

    /** Publica una nueva versión, que pasa a ser la vigente. */
    void publicar(PoliticaCancelacion politica);
}
