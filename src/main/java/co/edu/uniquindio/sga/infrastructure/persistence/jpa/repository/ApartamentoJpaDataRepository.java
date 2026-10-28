package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApartamentoJpaDataRepository extends JpaRepository<ApartamentoEntity, Long> {

    Optional<ApartamentoEntity> findByIdentificacion(String identificacion);

    boolean existsByIdentificacion(String identificacion);

    List<ApartamentoEntity> findByActivoTrueAndCapacidadGreaterThanEqual(int totalOcupantes);
}
