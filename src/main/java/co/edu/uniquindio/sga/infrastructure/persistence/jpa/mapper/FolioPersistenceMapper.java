package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CargoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PagoEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/** Mapper dominio ↔ entidad JPA del folio. */
@Mapper(componentModel = "spring")
public interface FolioPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigoReserva", source = "reserva.valor")
    @Mapping(target = "cerrado", expression = "java(folio.estaCerrado())")
    @Mapping(target = "alojamientoLiquidado", expression = "java(folio.alojamientoLiquidado())")
    @Mapping(target = "cargos", expression = "java(folio.obtenerCargos().stream().map(this::toCargoEmbeddable).toList())")
    @Mapping(target = "pagos", expression = "java(folio.obtenerPagos().stream().map(this::toPagoEmbeddable).toList())")
    FolioEntity toEntity(Folio folio);

    CargoEmbeddable toCargoEmbeddable(Cargo cargo);

    default PagoEmbeddable toPagoEmbeddable(Pago pago) {
        return new PagoEmbeddable(pago.getIdentificacion(), pago.valor().valor(), pago.medio(),
                pago.momento(), pago.concepto(), pago.autor());
    }

    default BigDecimal toBigDecimal(Dinero dinero) {
        return dinero == null ? null : dinero.valor();
    }

    default Folio toDomain(FolioEntity entity) {
        if (entity == null) {
            return null;
        }
        return Folio.reconstruir(
                new CodigoReserva(entity.getCodigoReserva()),
                entity.getCargos().stream()
                        .map(c -> new Cargo(c.getTipo(), c.getConcepto(), new Dinero(c.getValor()),
                                c.getMomento(), c.getAutor()))
                        .toList(),
                entity.getPagos().stream()
                        .map(p -> new Pago(p.getIdentificacion(), new Dinero(p.getValor()), p.getMedio(),
                                p.getMomento(), p.getConcepto(), p.getAutor()))
                        .toList(),
                entity.isCerrado(),
                entity.isAlojamientoLiquidado(),
                entity.getAutorizacionCierre());
    }
}
