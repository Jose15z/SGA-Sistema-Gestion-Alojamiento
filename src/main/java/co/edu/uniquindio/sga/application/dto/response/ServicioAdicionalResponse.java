package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.UnidadCobro;

/** Servicio adicional del alojamiento. */
public record ServicioAdicionalResponse(
        String codigo,
        String nombre,
        boolean generaCargo,
        Long valor,
        UnidadCobro unidad
) {
}
