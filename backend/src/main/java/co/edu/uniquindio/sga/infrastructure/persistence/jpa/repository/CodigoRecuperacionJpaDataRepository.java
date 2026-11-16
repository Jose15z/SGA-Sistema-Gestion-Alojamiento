package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CodigoRecuperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CodigoRecuperacionJpaDataRepository extends JpaRepository<CodigoRecuperacionEntity, UUID> {

    List<CodigoRecuperacionEntity> findByCorreoAndUsadoFalse(String correo);
}
