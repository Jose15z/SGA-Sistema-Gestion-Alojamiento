package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.TarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDisponibleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.TarifaResponse;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Mapper DTO ↔ dominio de apartamentos y tarifas. */
@Mapper(componentModel = "spring", uses = ValorRestMapper.class)
public interface ApartamentoRestMapper {

    @Mapping(target = "identificacion", source = "identificacion.valor")
    @Mapping(target = "capacidad", source = "capacidad.valor")
    @Mapping(target = "activo", expression = "java(apartamento.estaActivo())")
    ApartamentoResponse toResponse(Apartamento apartamento);

    @Mapping(target = "identificacion", source = "identificacion.valor")
    @Mapping(target = "capacidadMaxima", source = "capacidad.valor")
    ApartamentoDisponibleResponse toDisponible(Apartamento apartamento);

    TarifaResponse toTarifaResponse(Tarifa tarifa);

    default List<Tarifa> toTarifas(IdentificacionApartamento apartamento, List<TarifaRequest> tarifas) {
        return tarifas.stream()
                .map(tarifa -> new Tarifa(apartamento, tarifa.temporada(), Dinero.de(tarifa.valorPorOcupanteNoche())))
                .toList();
    }
}
