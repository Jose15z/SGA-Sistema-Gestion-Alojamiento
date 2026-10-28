package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.util.Objects;

/**
 * RN-14 y RN-22: toda modificación de fechas revalida las condiciones de
 * creación que dependen de otros agregados y recalcula el valor.
 *
 * <p>La reserva no conoce las demás reservas, los bloqueos ni las tarifas;
 * por eso la decisión cruza agregados. La propia reserva aplica después el
 * cambio con {@code modificarEstancia}, que conserva la política congelada.</p>
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

    /**
     * @return el nuevo valor que debe congelarse en la reserva
     * @throws ReglaDominioException si la nueva estancia no es vendible
     */
    public ValorCongelado validarModificacion(Reserva reserva, Apartamento apartamento, Estancia nuevaEstancia) {
        if (reserva == null || apartamento == null || nuevaEstancia == null) {
            throw new ReglaDominioException("La modificación requiere reserva, apartamento y nueva estancia");
        }
        if (!reserva.getApartamento().equals(apartamento.getIdentificacion())) {
            throw new ReglaDominioException("El apartamento no corresponde a la reserva");
        }
        // RN-01, RN-02, RN-07, RN-20, excluyendo la propia reserva
        disponibilidad.verificarDisponible(apartamento, nuevaEstancia, reserva.totalOcupantes(), reserva.getCodigo());
        // RN-05, RN-06, RP-03
        ValorCongelado nuevoValor = cotizador.cotizar(apartamento.getIdentificacion(), nuevaEstancia,
                reserva.getOcupantes());
        // RP-01
        estanciaMinima.verificar(nuevoValor);
        return nuevoValor;
    }
}
