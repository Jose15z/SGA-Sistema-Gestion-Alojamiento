package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.TarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDisponibleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.NocheCalendarioResponse;
import co.edu.uniquindio.sga.application.dto.response.NovedadApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.RegistroTarifaResponse;
import co.edu.uniquindio.sga.application.dto.response.TarifaResponse;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import co.edu.uniquindio.sga.domain.valueobject.NovedadApartamento;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
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

    default ApartamentoDisponibleResponse toDisponible(BuscarApartamentosDisponiblesUseCase.Resultado resultado) {
        Apartamento apartamento = resultado.apartamento();
        return new ApartamentoDisponibleResponse(apartamento.getIdentificacion().valor(), apartamento.getNombre(),
                apartamento.getDescripcion(), apartamento.getDormitorios(), apartamento.getCapacidad().valor(),
                apartamento.getCaracteristicas(), resultado.valorEstimado().noches(),
                resultado.valorEstimado().total().valor().longValueExact());
    }

    TarifaResponse toTarifaResponse(Tarifa tarifa);

    @Mapping(target = "valorPorOcupanteNoche", source = "valor")
    RegistroTarifaResponse toRegistroTarifa(RegistroTarifa registro);

    NovedadApartamentoResponse toNovedad(NovedadApartamento novedad);

    /** La reserva que ocupa la noche solo se muestra al personal del alojamiento. */
    default NocheCalendarioResponse toNoche(NocheCalendario noche, boolean mostrarReserva) {
        return new NocheCalendarioResponse(noche.noche(), noche.estado(),
                mostrarReserva && noche.reserva() != null ? noche.reserva().valor() : null);
    }

    default List<Tarifa> toTarifas(IdentificacionApartamento apartamento, List<TarifaRequest> tarifas) {
        return tarifas.stream()
                .map(tarifa -> new Tarifa(apartamento, tarifa.temporada(), Dinero.de(tarifa.valorPorOcupanteNoche())))
                .toList();
    }
}
