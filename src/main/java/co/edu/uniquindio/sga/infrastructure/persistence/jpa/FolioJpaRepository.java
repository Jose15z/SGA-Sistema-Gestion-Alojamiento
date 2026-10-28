package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.FolioPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.FolioJpaDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Adaptador JPA del puerto FolioRepository. */
@Repository
@Transactional
public class FolioJpaRepository implements FolioRepository {

    private final FolioJpaDataRepository dataRepository;
    private final FolioPersistenceMapper mapper;

    public FolioJpaRepository(FolioJpaDataRepository dataRepository, FolioPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Folio> obtenerPorReserva(CodigoReserva codigo) {
        return dataRepository.findByCodigoReserva(codigo.valor()).map(mapper::toDomain);
    }

    @Override
    public void guardar(Folio folio) {
        FolioEntity entity = mapper.toEntity(folio);
        dataRepository.findByCodigoReserva(entity.getCodigoReserva())
                .ifPresent(existente -> entity.setId(existente.getId()));
        dataRepository.save(entity);
    }
}
