package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.AlojamientoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.AlojamientoJpaDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Adaptador JPA del puerto AlojamientoRepository. */
@Repository
@Transactional
public class AlojamientoJpaRepository implements AlojamientoRepository {

    private final AlojamientoJpaDataRepository dataRepository;
    private final AlojamientoPersistenceMapper mapper;

    public AlojamientoJpaRepository(AlojamientoJpaDataRepository dataRepository, AlojamientoPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alojamiento> obtener() {
        return dataRepository.findFirstByOrderByNombreAsc().map(mapper::toDomain);
    }

    @Override
    public void guardar(Alojamiento alojamiento) {
        dataRepository.save(mapper.toEntity(alojamiento));
    }
}
