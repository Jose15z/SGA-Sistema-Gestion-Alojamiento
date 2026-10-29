package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NovedadApartamento;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadApartamentoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifaEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;

/** Mapper dominio ↔ entidad JPA del apartamento (Guía 10, Ejercicio 1). */
@Mapper(componentModel = "spring")
public interface ApartamentoPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identificacion", source = "identificacion.valor")
    @Mapping(target = "capacidad", source = "capacidad.valor")
    @Mapping(target = "activo", expression = "java(apartamento.estaActivo())")
    @Mapping(target = "caracteristicas", expression = "java(new ArrayList<>(apartamento.getCaracteristicas()))")
    @Mapping(target = "tarifas", source = "historialTarifas")
    @Mapping(target = "novedades", source = "novedades")
    ApartamentoEntity toEntity(Apartamento apartamento);

    default TarifaEmbeddable toTarifaEmbeddable(RegistroTarifa registro) {
        return new TarifaEmbeddable(registro.temporada(), registro.valor().valor(), registro.vigenteDesde());
    }

    default NovedadApartamentoEmbeddable toNovedadEmbeddable(NovedadApartamento novedad) {
        return new NovedadApartamentoEmbeddable(novedad.fecha(), novedad.autor(), novedad.descripcion(),
                novedad.gravedad());
    }

    default Apartamento toDomain(ApartamentoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Apartamento.reconstruir(
                new IdentificacionApartamento(entity.getIdentificacion()),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getDormitorios(),
                new Capacidad(entity.getCapacidad()),
                new ArrayList<>(entity.getCaracteristicas()),
                entity.getEstadoOperativo(),
                entity.isActivo(),
                entity.getTarifas().stream()
                        .map(tarifa -> new RegistroTarifa(tarifa.getTemporada(),
                                new Dinero(tarifa.getValorPorOcupanteNoche()), tarifa.getVigenteDesde()))
                        .toList(),
                entity.getNovedades().stream()
                        .map(novedad -> new NovedadApartamento(novedad.getFecha(), novedad.getAutor(),
                                novedad.getDescripcion(), novedad.getGravedad()))
                        .toList());
    }
}
