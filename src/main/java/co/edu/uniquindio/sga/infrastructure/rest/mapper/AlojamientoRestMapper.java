package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.ServicioAdicionalRequest;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.ServicioAdicionalResponse;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import org.mapstruct.Mapper;

import java.util.List;

/** Mapper DTO ↔ dominio del alojamiento y su catálogo de servicios. */
@Mapper(componentModel = "spring", uses = ValorRestMapper.class)
public interface AlojamientoRestMapper {

    ServicioAdicionalResponse toServicioResponse(ServicioAdicional servicio);

    default AlojamientoResponse toResponse(Alojamiento alojamiento, TiempoPreparacion horarios) {
        return new AlojamientoResponse(alojamiento.getNombre(), alojamiento.getDescripcion(), alojamiento.getCiudad(),
                alojamiento.getDireccion(), alojamiento.getUbicacion().latitud(), alojamiento.getUbicacion().longitud(),
                horarios.horaEntrada(), horarios.horaSalida(), alojamiento.getNormas(),
                alojamiento.getServicios().stream().map(this::toServicioResponse).toList());
    }

    default List<ServicioAdicional> toServicios(List<ServicioAdicionalRequest> servicios) {
        return servicios.stream()
                .map(s -> new ServicioAdicional(s.codigo(), s.nombre(), s.generaCargo(), Dinero.de(s.valor()), s.unidad()))
                .toList();
    }
}
