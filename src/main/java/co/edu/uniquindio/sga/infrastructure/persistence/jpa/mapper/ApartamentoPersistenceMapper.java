package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifaEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/** Mapper dominio ↔ entidad JPA del apartamento (Guía 10, Ejercicio 1). */
@Mapper(componentModel = "spring")
public interface ApartamentoPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identificacion", source = "identificacion.valor")
    @Mapping(target = "capacidad", source = "capacidad.valor")
    @Mapping(target = "activo", expression = "java(apartamento.estaActivo())")
    ApartamentoEntity toEntity(Apartamento apartamento);

    TarifaEmbeddable toTarifaEmbeddable(Tarifa tarifa);

    default BigDecimal toBigDecimal(Dinero dinero) {
        return dinero == null ? null : dinero.valor();
    }

    default Apartamento toDomain(ApartamentoEntity entity) {
        if (entity == null) {
            return null;
        }
        IdentificacionApartamento identificacion = new IdentificacionApartamento(entity.getIdentificacion());
        return Apartamento.reconstruir(
                identificacion,
                entity.getNombre(),
                entity.getDormitorios(),
                new Capacidad(entity.getCapacidad()),
                entity.getEstadoOperativo(),
                entity.isActivo(),
                entity.getTarifas().stream()
                        .map(tarifa -> new Tarifa(identificacion, tarifa.getTemporada(),
                                new Dinero(tarifa.getValorPorOcupanteNoche())))
                        .toList());
    }
}
