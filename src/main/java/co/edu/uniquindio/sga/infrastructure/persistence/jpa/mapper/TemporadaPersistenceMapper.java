package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.valueobject.Temporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper dominio ↔ entidad JPA de la temporada (record ↔ entidad, ambos sentidos con MapStruct). */
@Mapper(componentModel = "spring")
public interface TemporadaPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    TemporadaEntity toEntity(Temporada temporada);

    Temporada toDomain(TemporadaEntity entity);
}
