package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.PoliticaCancelacionPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.PoliticaCancelacionJpaDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador JPA del puerto PoliticaCancelacionRepository. Publicar una versión
 * la marca vigente y deja las anteriores como históricas; nunca se sobrescriben.
 */
@Repository
@Transactional
public class PoliticaCancelacionJpaRepository implements PoliticaCancelacionRepository {

    private final PoliticaCancelacionJpaDataRepository dataRepository;
    private final PoliticaCancelacionPersistenceMapper mapper;

    public PoliticaCancelacionJpaRepository(PoliticaCancelacionJpaDataRepository dataRepository,
                                            PoliticaCancelacionPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PoliticaCancelacion obtenerVigente() {
        return dataRepository.findByVigenteTrue().map(mapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No hay una política de cancelación vigente"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version) {
        return dataRepository.findByVersion(version.numero()).map(mapper::toDomain);
    }

    @Override
    public void publicar(PoliticaCancelacion politica) {
        if (dataRepository.findByVersion(politica.getVersion().numero()).isPresent()) {
            throw new IllegalStateException("La versión " + politica.getVersion().numero() + " ya fue publicada");
        }
        dataRepository.findByVigenteTrue().ifPresent(anterior -> {
            anterior.setVigente(false);
            dataRepository.save(anterior);
        });
        PoliticaCancelacionEntity entity = mapper.toEntity(politica);
        entity.setVigente(true);
        dataRepository.save(entity);
    }
}
