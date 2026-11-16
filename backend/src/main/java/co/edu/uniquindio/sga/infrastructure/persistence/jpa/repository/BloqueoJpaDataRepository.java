package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BloqueoJpaDataRepository extends JpaRepository<BloqueoEntity, Long> {

    Optional<BloqueoEntity> findByIdentificacion(UUID identificacion);

    List<BloqueoEntity> findByApartamentoIdAndVigenteTrue(String apartamentoId);

    Page<BloqueoEntity> findByApartamentoId(String apartamentoId, Pageable pageable);
}
