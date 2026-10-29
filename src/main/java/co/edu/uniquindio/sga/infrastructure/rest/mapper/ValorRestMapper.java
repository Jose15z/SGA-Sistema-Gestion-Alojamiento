package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.CargoNocheResponse;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.function.Function;

/**
 * Conversiones compartidas: dinero en pesos enteros, cotizaciones y páginas.
 * Sin lógica: los cálculos (subtotal, total, descuento) los hace el dominio.
 */
@Mapper(componentModel = "spring")
public interface ValorRestMapper {

    default Long toPesos(Dinero dinero) {
        return dinero == null ? null : dinero.valor().longValueExact();
    }

    default Dinero toDinero(Long pesos) {
        return pesos == null ? null : Dinero.de(pesos);
    }

    @Mapping(target = "tarifaOcupanteNoche", source = "tarifaPorOcupante")
    @Mapping(target = "subtotalNoche", expression = "java(toPesos(cargo.subtotal()))")
    CargoNocheResponse toCargoNoche(CargoNoche cargo);

    default CotizacionResponse toCotizacion(ValorCongelado valor) {
        if (valor == null) {
            return null;
        }
        List<CargoNocheResponse> detalle = valor.detalle().stream().map(this::toCargoNoche).toList();
        return new CotizacionResponse(toPesos(valor.subtotal()), valor.porcentajeDescuento(),
                toPesos(valor.descuento()), toPesos(valor.total()), valor.noches(), detalle);
    }

    default <D, R> PaginaResponse<R> toPagina(Pagina<D> pagina, Function<D, R> conversion) {
        return new PaginaResponse<>(pagina.contenido().stream().map(conversion).toList(),
                pagina.numero(), pagina.tamanio(), pagina.totalElementos(), pagina.totalPaginas());
    }
}
