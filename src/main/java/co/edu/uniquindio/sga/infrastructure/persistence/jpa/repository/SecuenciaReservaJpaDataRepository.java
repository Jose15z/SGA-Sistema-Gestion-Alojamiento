package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.SecuenciaReservaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SecuenciaReservaJpaDataRepository extends JpaRepository<SecuenciaReservaEntity, Integer> {

    /** Bloquea la fila del año para que dos reservas simultáneas no reciban el mismo código. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SecuenciaReservaEntity s where s.anio = :anio")
    Optional<SecuenciaReservaEntity> bloquearPorAnio(@Param("anio") Integer anio);
}
