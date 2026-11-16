package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PoliticaCancelacionJpaDataRepository extends JpaRepository<PoliticaCancelacionEntity, Long> {

    Optional<PoliticaCancelacionEntity> findByVigenteTrue();

    Optional<PoliticaCancelacionEntity> findByVersion(int version);

    List<PoliticaCancelacionEntity> findByVigenteTrueAndVersionNot(int version);
}
