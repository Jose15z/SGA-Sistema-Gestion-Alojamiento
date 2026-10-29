package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApartamentoJpaDataRepository extends JpaRepository<ApartamentoEntity, Long> {

    Optional<ApartamentoEntity> findByIdentificacion(String identificacion);

    boolean existsByIdentificacion(String identificacion);

    @Query("""
            select a from ApartamentoEntity a
            where (:estado is null or a.estadoOperativo = :estado)
              and (:incluirRetirados = true or a.activo = true)
            """)
    Page<ApartamentoEntity> listar(@Param("estado") EstadoOperativo estado,
                                   @Param("incluirRetirados") boolean incluirRetirados, Pageable pageable);

    List<ApartamentoEntity> findByActivoTrueAndCapacidadGreaterThanEqual(int totalOcupantes);
}
