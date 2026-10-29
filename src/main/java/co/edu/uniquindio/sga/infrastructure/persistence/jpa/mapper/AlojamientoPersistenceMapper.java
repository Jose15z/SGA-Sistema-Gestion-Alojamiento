package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Ubicacion;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ServicioAdicionalEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;

/** Mapper dominio ↔ entidad JPA del alojamiento. */
@Mapper(componentModel = "spring")
public interface AlojamientoPersistenceMapper {

    @Mapping(target = "latitud", source = "ubicacion.latitud")
    @Mapping(target = "longitud", source = "ubicacion.longitud")
    @Mapping(target = "normas", expression = "java(new ArrayList<>(alojamiento.getNormas()))")
    AlojamientoEntity toEntity(Alojamiento alojamiento);

    default ServicioAdicionalEmbeddable toServicioEmbeddable(ServicioAdicional servicio) {
        return new ServicioAdicionalEmbeddable(servicio.codigo(), servicio.nombre(), servicio.generaCargo(),
                servicio.valor().valor(), servicio.unidad());
    }

    default Alojamiento toDomain(AlojamientoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Alojamiento(entity.getIdentificacion(), entity.getNombre(), entity.getDescripcion(),
                entity.getCiudad(), entity.getDireccion(), new Ubicacion(entity.getLatitud(), entity.getLongitud()),
                new ArrayList<>(entity.getNormas()),
                entity.getServicios().stream()
                        .map(s -> new ServicioAdicional(s.getCodigo(), s.getNombre(), s.isGeneraCargo(),
                                new Dinero(s.getValor()), s.getUnidad()))
                        .toList());
    }
}
