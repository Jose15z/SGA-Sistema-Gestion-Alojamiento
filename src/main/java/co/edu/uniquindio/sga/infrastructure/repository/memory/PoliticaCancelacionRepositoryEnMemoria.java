package co.edu.uniquindio.sga.infrastructure.repository.memory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Archivo de versiones inmutables. Publicar una versión nunca reemplaza una anterior. */
public final class PoliticaCancelacionRepositoryEnMemoria implements PoliticaCancelacionRepository {
    private final Map<VersionPolitica, PoliticaCancelacion> datos = new LinkedHashMap<>();
    private VersionPolitica vigente;
    public PoliticaCancelacionRepositoryEnMemoria(PoliticaCancelacion inicial) { publicar(inicial); }
    public void publicar(PoliticaCancelacion politica) {
        Objects.requireNonNull(politica);
        if (datos.containsKey(politica.getVersion())) throw new IllegalArgumentException("La versión ya existe");
        datos.put(politica.getVersion(), politica);
        vigente = politica.getVersion();
    }
    @Override public PoliticaCancelacion obtenerVigente() { return datos.get(vigente); }
    @Override public Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version) {
        return Optional.ofNullable(datos.get(Objects.requireNonNull(version)));
    }
}
