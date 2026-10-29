package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlojamientoJpaDataRepository extends JpaRepository<AlojamientoEntity, UUID> {

    Optional<AlojamientoEntity> findFirstByOrderByNombreAsc();
}
