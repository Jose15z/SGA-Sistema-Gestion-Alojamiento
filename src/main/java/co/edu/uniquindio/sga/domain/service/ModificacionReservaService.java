package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 7.5, RN-14 y RN-22: toda modificación (fechas, ocupantes o apartamento) repite
 * las seis validaciones de creación y recalcula el valor con las tarifas vigentes.
 *
 * <p>La reserva no conoce las demás reservas, los bloqueos ni las tarifas;
 * por eso la decisión cruza agregados. La propia reserva aplica después el
 * cambio con {@code modificar}, que conserva la política congelada.</p>
 */
public final class ModificacionReservaService {

    private final DisponibilidadApartamentoService disponibilidad;
    private final CotizadorEstanciaService cotizador;
    private final EstanciaMinimaTemporadaService estanciaMinima;

    public ModificacionReservaService(DisponibilidadApartamentoService disponibilidad,
                                      CotizadorEstanciaService cotizador,
                                      EstanciaMinimaTemporadaService estanciaMinima) {
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
        this.cotizador = Objects.requireNonNull(cotizador);
        this.estanciaMinima = Objects.requireNonNull(estanciaMinima);
    }

    /** Solo cambian las fechas: mismo apartamento y mismo grupo. */
    public ValorCongelado validarModificacion(Reserva reserva, Apartamento apartamento, Estancia nuevaEstancia,
                                              LocalDate hoy) {
        if (reserva == null) {
            throw new ReglaDominioException("La modificación requiere la reserva");
        }
        return validarModificacion(reserva, apartamento, nuevaEstancia, reserva.getOcupantes(), hoy);
    }

    /**
     * @param destino         apartamento asignado tras la modificación (el mismo u otro)
     * @param nuevosOcupantes grupo completo tras la modificación
     * @param hoy             fecha actual para la validación (2) de 7.5
     * @return el nuevo valor que debe congelarse en la reserva
     * @throws ReglaDominioException con la primera validación que no se cumple
     */
    public ValorCongelado validarModificacion(Reserva reserva, Apartamento destino, Estancia nuevaEstancia,
                                              List<Ocupante> nuevosOcupantes, LocalDate hoy) {
        if (reserva == null || destino == null || nuevaEstancia == null || nuevosOcupantes == null) {
            throw new ReglaDominioException("La modificación requiere reserva, apartamento, estancia y grupo");
        }
        // (1) a (6) de 7.5, sin que la reserva compita consigo misma
        disponibilidad.verificarDisponible(destino, nuevaEstancia, nuevosOcupantes.size(), reserva.getCodigo(), hoy);
        // RN-05, RN-06, RP-03: valor con las tarifas vigentes
        ValorCongelado nuevoValor = cotizador.cotizar(destino.getIdentificacion(), nuevaEstancia, nuevosOcupantes);
        // RP-01
        estanciaMinima.verificar(nuevoValor);
        return nuevoValor;
    }
}
