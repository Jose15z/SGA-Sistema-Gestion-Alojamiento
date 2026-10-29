package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CalendarioTemporadas;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TemporadaPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TemporadaJpaDataRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA del puerto TemporadaRepository. */
@Repository
@Transactional
public class TemporadaJpaRepository implements TemporadaRepository {

    private final TemporadaJpaDataRepository dataRepository;
    private final TemporadaPersistenceMapper mapper;

    public TemporadaJpaRepository(TemporadaJpaDataRepository dataRepository, TemporadaPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarioTemporadas obtenerCalendario() {
        return new CalendarioTemporadas(dataRepository.findAll().stream().map(mapper::toDomain).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Temporada> listar(PeticionPagina pagina) {
        return PaginacionJpa.aPagina(dataRepository.findAll(
                PaginacionJpa.aPageable(pagina, Sort.by("fechaInicio"))), mapper::toDomain);
    }

    @Override
    public void guardar(Temporada temporada) {
        dataRepository.save(mapper.toEntity(temporada));
    }
}
