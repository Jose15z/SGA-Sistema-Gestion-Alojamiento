package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import java.util.List;

/** Detalle de un apartamento. */
public record ApartamentoResponse(
        String identificacion,
        String nombre,
        String descripcion,
        int dormitorios,
        int capacidad,
        List<String> caracteristicas,
        EstadoOperativo estadoOperativo,
        boolean activo,
        List<TarifaResponse> tarifas
) {
}
