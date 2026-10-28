package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ApartamentoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ApartamentoJpaDataRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ApartamentoRepository (Guía 10, Ejercicio 1). */
@Repository
@Transactional
public class ApartamentoJpaRepository implements ApartamentoRepository {

    private final ApartamentoJpaDataRepository dataRepository;
    private final ApartamentoPersistenceMapper mapper;

    public ApartamentoJpaRepository(ApartamentoJpaDataRepository dataRepository, ApartamentoPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Apartamento> obtenerPorIdentificacion(IdentificacionApartamento identificacion) {
        return dataRepository.findByIdentificacion(identificacion.valor()).map(mapper::toDomain);
    }

    @Override
    public void guardar(Apartamento apartamento) {
        ApartamentoEntity entity = mapper.toEntity(apartamento);
        dataRepository.findByIdentificacion(entity.getIdentificacion())
                .ifPresent(existente -> entity.setId(existente.getId()));
        dataRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Apartamento> listar(PeticionPagina pagina) {
        return PaginacionJpa.aPagina(dataRepository.findAll(
                PaginacionJpa.aPageable(pagina, Sort.by("identificacion"))), mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apartamento> buscarActivosConCapacidadPara(int totalOcupantes) {
        return dataRepository.findByActivoTrueAndCapacidadGreaterThanEqual(totalOcupantes).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(IdentificacionApartamento identificacion) {
        return dataRepository.existsByIdentificacion(identificacion.valor());
    }
}
