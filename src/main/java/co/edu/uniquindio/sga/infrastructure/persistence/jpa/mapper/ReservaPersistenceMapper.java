package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CargoNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EventoReservaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.OcupanteEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.VehiculoEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper dominio ↔ entidad JPA de la reserva (Guía 10, Parte 4).
 * Dominio → entidad lo genera MapStruct; entidad → dominio usa
 * {@link Reserva#reconstruir}, porque la reserva no tiene setters.
 */
@Mapper(componentModel = "spring")
public interface ReservaPersistenceMapper {

    // ================= DOMINIO → ENTIDAD (MapStruct genera el código) =================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", source = "codigo.valor")
    @Mapping(target = "apartamentoId", source = "apartamento.valor")
    @Mapping(target = "identificadorExterno", source = "identificadorExterno.valor")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion.momento")
    @Mapping(target = "titularDocumento", source = "titular.documento.numero")
    @Mapping(target = "cargosNoche", source = "valor.detalle")
    @Mapping(target = "porcentajeDescuento", source = "valor.porcentajeDescuento")
    @Mapping(target = "politicaVersion", source = "politica.numero")
    ReservaEntity toEntity(Reserva reserva);

    @Mapping(target = "documento", source = "documento.numero")
    OcupanteEmbeddable toOcupanteEmbeddable(Ocupante ocupante);

    EstanciaEmbeddable toEstanciaEmbeddable(Estancia estancia);

    CargoNocheEmbeddable toCargoNocheEmbeddable(CargoNoche cargo);

    EventoReservaEmbeddable toEventoEmbeddable(EventoReserva evento);

    VehiculoEmbeddable toVehiculoEmbeddable(Vehiculo vehiculo);

    default BigDecimal toBigDecimal(Dinero dinero) {
        return dinero == null ? null : dinero.valor();
    }

    // ================= ENTIDAD → DOMINIO (método default escrito a mano) =================

    default Reserva toDomain(ReservaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<Ocupante> ocupantes = entity.getOcupantes().stream().map(this::toOcupante).toList();
        Ocupante titular = ocupantes.stream()
                .filter(ocupante -> ocupante.getDocumento().numero().equals(entity.getTitularDocumento()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El titular no está entre los ocupantes de la reserva " + entity.getCodigo()));
        List<CargoNoche> detalle = entity.getCargosNoche().stream()
                .map(cargo -> new CargoNoche(cargo.getNoche(), cargo.getTemporada(),
                        new Dinero(cargo.getTarifaPorOcupante()), cargo.getOcupantesFacturables()))
                .toList();
        List<EventoReserva> historial = entity.getHistorial().stream()
                .map(evento -> new EventoReserva(evento.getMomento(), evento.getAccion(), evento.getAutor(),
                        evento.getEstadoAnterior(), evento.getEstadoNuevo(), evento.getObservacion()))
                .toList();
        return Reserva.reconstruir(
                new CodigoReserva(entity.getCodigo()),
                new IdentificacionApartamento(entity.getApartamentoId()),
                toEstancia(entity.getEstancia()),
                titular,
                ocupantes,
                entity.getCanalOrigen(),
                entity.getIdentificadorExterno() == null ? null
                        : new IdentificadorExterno(entity.getIdentificadorExterno()),
                new FechaCreacion(entity.getFechaCreacion()),
                new ValorCongelado(detalle, entity.getPorcentajeDescuento()),
                new VersionPolitica(entity.getPoliticaVersion()),
                entity.getEstado(),
                historial,
                entity.getHoraEstimadaLlegada(),
                entity.getNovedadLlegada(),
                entity.getMascotas(),
                entity.getVehiculos().stream().map(v -> new Vehiculo(v.getPlaca())).toList());
    }

    default Ocupante toOcupante(OcupanteEmbeddable embeddable) {
        return Ocupante.reconstruir(new DocumentoIdentidad(embeddable.getDocumento()),
                embeddable.getNombre(), embeddable.getFechaNacimiento());
    }

    default Estancia toEstancia(EstanciaEmbeddable embeddable) {
        return new Estancia(embeddable.getFechaEntrada(), embeddable.getFechaSalida());
    }
}
