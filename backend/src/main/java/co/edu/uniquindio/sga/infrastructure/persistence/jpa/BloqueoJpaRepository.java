package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.BloqueoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.BloqueoJpaDataRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador JPA del puerto BloqueoRepository. */
@Repository
@Transactional
public class BloqueoJpaRepository implements BloqueoRepository {

    private final BloqueoJpaDataRepository dataRepository;
    private final BloqueoPersistenceMapper mapper;

    public BloqueoJpaRepository(BloqueoJpaDataRepository dataRepository, BloqueoPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento) {
        return dataRepository.findByApartamentoIdAndVigenteTrue(apartamento.valor()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bloqueo> obtenerPorIdentificacion(UUID identificacion) {
        return dataRepository.findByIdentificacion(identificacion).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Bloqueo> listarPorApartamento(IdentificacionApartamento apartamento, PeticionPagina pagina) {
        Sort orden = Sort.by(Sort.Order.desc("periodo.fechaEntrada"));
        return PaginacionJpa.aPagina(dataRepository.findByApartamentoId(apartamento.valor(),
                PaginacionJpa.aPageable(pagina, orden)), mapper::toDomain);
    }

    @Override
    public void guardar(Bloqueo bloqueo) {
        BloqueoEntity entity = mapper.toEntity(bloqueo);
        dataRepository.findByIdentificacion(entity.getIdentificacion())
                .ifPresent(existente -> entity.setId(existente.getId()));
        dataRepository.save(entity);
    }
}
