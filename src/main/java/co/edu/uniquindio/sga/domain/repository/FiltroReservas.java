package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.time.LocalDate;

/**
 * Filtros opcionales del listado de reservas (7.5). Un campo null no filtra.
 *
 * @param estado       estado del ciclo de vida
 * @param apartamento  apartamento reservado
 * @param titular      documento del titular (el huésped solo ve las suyas)
 * @param entradaDesde fecha de entrada mínima (inclusive)
 * @param entradaHasta fecha de entrada máxima (inclusive)
 */
public record FiltroReservas(
        EstadoReserva estado,
        IdentificacionApartamento apartamento,
        DocumentoIdentidad titular,
        LocalDate entradaDesde,
        LocalDate entradaHasta
) {

    public FiltroReservas soloDelTitular(DocumentoIdentidad documento) {
        return new FiltroReservas(estado, apartamento, documento, entradaDesde, entradaHasta);
    }
}
