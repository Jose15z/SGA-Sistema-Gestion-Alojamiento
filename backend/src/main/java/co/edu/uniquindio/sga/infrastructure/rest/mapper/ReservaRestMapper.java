package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.OcupanteCotizacionRequest;
import co.edu.uniquindio.sga.application.dto.request.OcupanteRequest;
import co.edu.uniquindio.sga.application.dto.response.EventoHistorialResponse;
import co.edu.uniquindio.sga.application.dto.response.OcupanteResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/** Mapper DTO ↔ dominio de las reservas (Guía 08). */
@Mapper(componentModel = "spring", uses = ValorRestMapper.class)
public interface ReservaRestMapper {

    @Mapping(target = "codigo", source = "codigo.valor")
    @Mapping(target = "identificacionApartamento", source = "apartamento.valor")
    @Mapping(target = "fechaEntrada", source = "estancia.fechaEntrada")
    @Mapping(target = "fechaSalida", source = "estancia.fechaSalida")
    @Mapping(target = "identificadorExterno", source = "identificadorExterno.valor")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion.momento")
    @Mapping(target = "cotizacionCongelada", source = "valor")
    @Mapping(target = "versionPolitica", source = "politica.numero")
    ReservaDetalleResponse toDetalle(Reserva reserva);

    @Mapping(target = "codigo", source = "codigo.valor")
    @Mapping(target = "identificacionApartamento", source = "apartamento.valor")
    @Mapping(target = "fechaEntrada", source = "estancia.fechaEntrada")
    @Mapping(target = "fechaSalida", source = "estancia.fechaSalida")
    @Mapping(target = "titularNombre", source = "titular.nombre")
    @Mapping(target = "valorTotal", expression = "java(valorRestMapper.toPesos(reserva.getValor().total()))")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion.momento")
    ReservaResumenResponse toResumen(Reserva reserva);

    @Mapping(target = "documento", source = "documento.numero")
    @Mapping(target = "nombreCompleto", source = "nombre")
    OcupanteResponse toOcupanteResponse(Ocupante ocupante);

    EventoHistorialResponse toEvento(EventoReserva evento);

    default String toPlaca(Vehiculo vehiculo) {
        return vehiculo.placa();
    }

    // ---------- Request → dominio ----------

    default Ocupante toOcupante(OcupanteRequest request, LocalDate hoy) {
        return new Ocupante(new DocumentoIdentidad(request.documento()), request.nombreCompleto(),
                request.fechaNacimiento(), hoy);
    }

    /** El grupo de la reserva: el titular más sus acompañantes. */
    default List<Ocupante> toGrupo(OcupanteRequest titular, List<OcupanteRequest> acompanantes, LocalDate hoy) {
        List<Ocupante> grupo = new ArrayList<>();
        grupo.add(toOcupante(titular, hoy));
        if (acompanantes != null) {
            acompanantes.forEach(acompanante -> grupo.add(toOcupante(acompanante, hoy)));
        }
        return grupo;
    }

    default IdentificadorExterno toIdentificadorExterno(String valor) {
        return valor == null || valor.isBlank() ? null : new IdentificadorExterno(valor);
    }

    /** Para cotizar solo importa la edad: cada integrante recibe una identificación provisional. */
    default List<Ocupante> toOcupantesCotizacion(List<OcupanteCotizacionRequest> ocupantes, LocalDate hoy) {
        return IntStream.range(0, ocupantes.size())
                .mapToObj(i -> new Ocupante(new DocumentoIdentidad("COTIZACION-" + (i + 1)),
                        "Ocupante " + (i + 1), ocupantes.get(i).fechaNacimiento(), hoy))
                .toList();
    }
}
