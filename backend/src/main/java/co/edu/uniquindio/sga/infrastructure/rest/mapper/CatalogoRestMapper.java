package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.request.TramoCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.dto.response.TramoCancelacionResponse;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

/** Mapper DTO ↔ dominio de temporadas, política de cancelación y bloqueos. */
@Mapper(componentModel = "spring")
public interface CatalogoRestMapper {

    Temporada toTemporada(RegistrarTemporadaRequest request);

    TemporadaResponse toTemporadaResponse(Temporada temporada);

    @Mapping(target = "version", source = "version.numero")
    PoliticaCancelacionResponse toPoliticaResponse(PoliticaCancelacion politica);

    @Mapping(target = "antelacionMinimaHoras", expression = "java(tramo.antelacionMinima().toHours())")
    TramoCancelacionResponse toTramoResponse(TramoCancelacion tramo);

    default TramoCancelacion toTramo(TramoCancelacionRequest request) {
        return new TramoCancelacion(Duration.ofHours(request.antelacionMinimaHoras()), request.porcentaje());
    }

    @Mapping(target = "identificacionApartamento", source = "apartamento.valor")
    @Mapping(target = "fechaEntrada", source = "periodo.fechaEntrada")
    @Mapping(target = "fechaSalida", source = "periodo.fechaSalida")
    @Mapping(target = "vigente", expression = "java(bloqueo.estaVigente())")
    BloqueoResponse toBloqueoResponse(Bloqueo bloqueo);
}
