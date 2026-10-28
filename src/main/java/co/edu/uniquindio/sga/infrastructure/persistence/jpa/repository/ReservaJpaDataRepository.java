package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Spring Data genera la implementación (Guía 10, 11). Vive en infraestructura, no en el dominio. */
public interface ReservaJpaDataRepository extends JpaRepository<ReservaEntity, Long> {

    Optional<ReservaEntity> findByCodigo(String codigo);

    List<ReservaEntity> findByEstado(EstadoReserva estado);

    List<ReservaEntity> findByApartamentoIdAndEstadoIn(String apartamentoId, Collection<EstadoReserva> estados);

    boolean existsByCanalOrigenAndIdentificadorExterno(CanalOrigen canal, String identificadorExterno);

    /** Prefiltro por fechas; el dominio vuelve a decidir el solapamiento con Estancia.seSolapaCon. */
    @Query("""
            select r from ReservaEntity r
            where r.estado in :estados
              and r.estancia.fechaEntrada < :salida
              and r.estancia.fechaSalida > :entrada
            """)
    List<ReservaEntity> buscarEnPeriodo(@Param("estados") Collection<EstadoReserva> estados,
                                        @Param("entrada") LocalDate entrada,
                                        @Param("salida") LocalDate salida);

    /** Listado con filtros opcionales (7.5); el orden lo define el Pageable. */
    @Query("""
            select r from ReservaEntity r
            where (:estado is null or r.estado = :estado)
              and (:apartamento is null or r.apartamentoId = :apartamento)
              and (:titular is null or r.titularDocumento = :titular)
              and (:desde is null or r.estancia.fechaEntrada >= :desde)
              and (:hasta is null or r.estancia.fechaEntrada <= :hasta)
            """)
    Page<ReservaEntity> buscar(@Param("estado") EstadoReserva estado,
                               @Param("apartamento") String apartamento,
                               @Param("titular") String titular,
                               @Param("desde") LocalDate desde,
                               @Param("hasta") LocalDate hasta,
                               Pageable pageable);
}
