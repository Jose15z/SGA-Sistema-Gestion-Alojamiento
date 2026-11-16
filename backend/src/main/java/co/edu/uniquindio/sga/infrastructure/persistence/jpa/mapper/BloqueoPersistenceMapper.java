package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EstanciaEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper dominio ↔ entidad JPA del bloqueo. */
@Mapper(componentModel = "spring")
public interface BloqueoPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apartamentoId", source = "apartamento.valor")
    @Mapping(target = "vigente", expression = "java(bloqueo.estaVigente())")
    BloqueoEntity toEntity(Bloqueo bloqueo);

    EstanciaEmbeddable toEstanciaEmbeddable(Estancia estancia);

    default Bloqueo toDomain(BloqueoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Bloqueo.reconstruir(entity.getIdentificacion(),
                new IdentificacionApartamento(entity.getApartamentoId()),
                new Estancia(entity.getPeriodo().getFechaEntrada(), entity.getPeriodo().getFechaSalida()),
                entity.getMotivo(), entity.isVigente(), entity.getMotivoLevantamiento());
    }
}
