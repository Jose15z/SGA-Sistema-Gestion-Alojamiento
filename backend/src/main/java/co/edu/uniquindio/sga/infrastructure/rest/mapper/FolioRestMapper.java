package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.CargoResponse;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.dto.response.PagoResponse;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper dominio → DTO del folio; el saldo lo calcula el dominio (RN-15). */
@Mapper(componentModel = "spring", uses = ValorRestMapper.class)
public interface FolioRestMapper {

    @Mapping(target = "codigoReserva", source = "reserva.valor")
    @Mapping(target = "saldo", expression = "java(valorRestMapper.toPesos(folio.saldo()))")
    @Mapping(target = "totalPagado", expression = "java(valorRestMapper.toPesos(folio.totalPagado()))")
    @Mapping(target = "cerrado", expression = "java(folio.estaCerrado())")
    @Mapping(target = "cargos", expression = "java(folio.obtenerCargos().stream().map(this::toCargoResponse).toList())")
    @Mapping(target = "pagos", expression = "java(folio.obtenerPagos().stream().map(this::toPagoResponse).toList())")
    FolioResponse toResponse(Folio folio);

    CargoResponse toCargoResponse(Cargo cargo);

    @Mapping(target = "valor", expression = "java(valorRestMapper.toPesos(pago.valor()))")
    @Mapping(target = "medio", expression = "java(pago.medio())")
    @Mapping(target = "momento", expression = "java(pago.momento())")
    @Mapping(target = "concepto", expression = "java(pago.concepto())")
    @Mapping(target = "autor", expression = "java(pago.autor())")
    PagoResponse toPagoResponse(Pago pago);
}
