package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TramoCancelacionEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

/** Mapper dominio ↔ entidad JPA de la política de cancelación. */
@Mapper(componentModel = "spring")
public interface PoliticaCancelacionPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vigente", ignore = true)
    @Mapping(target = "version", source = "version.numero")
    PoliticaCancelacionEntity toEntity(PoliticaCancelacion politica);

    @Mapping(target = "antelacionMinimaHoras", expression = "java(tramo.antelacionMinima().toHours())")
    TramoCancelacionEmbeddable toTramoEmbeddable(TramoCancelacion tramo);

    default PoliticaCancelacion toDomain(PoliticaCancelacionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PoliticaCancelacion(new VersionPolitica(entity.getVersion()),
                entity.getTramos().stream()
                        .map(t -> new TramoCancelacion(Duration.ofHours(t.getAntelacionMinimaHoras()), t.getPorcentaje()))
                        .toList(),
                entity.getPorcentajeNoShow(), entity.getHoraEntrada());
    }
}
