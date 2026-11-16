package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.util.Objects;

/** Autoriza la entrega cruzando Reserva y Apartamento (RN-10 y RN-11). */
public class EntregaApartamentoService {
    private final ApartamentoRepository apartamentos;

    public EntregaApartamentoService(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    /**
     * Verifica la fecha, el estado de la reserva y el apartamento.
     * Devuelve la misma instancia autorizada para que la aplicación
     * coordine su ocupación sin volver a consultarla.
     * No modifica ninguno de los agregados.
     *
     * @throws ReglaDominioException si no puede realizarse la entrega
     */
    public Apartamento autorizarEntrega(Reserva reserva, LocalDate fechaActual) {
        if (reserva == null || fechaActual == null) {
            throw new ReglaDominioException("La entrega requiere reserva y fecha actual");
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Solo puede entregarse una reserva CONFIRMADA");
        }
        if (fechaActual.isBefore(reserva.getEstancia().fechaEntrada())) {
            throw new ReglaDominioException("No se puede entregar antes de la fecha de entrada");
        }
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(reserva.getApartamento())
                .orElseThrow(() -> new ReglaDominioException(
                        "El apartamento de la reserva no existe"));
        if (!apartamento.getIdentificacion().equals(reserva.getApartamento())) {
            throw new ReglaDominioException("El apartamento consultado no corresponde a la reserva");
        }
        if (!apartamento.puedeRecibirGrupo()) {
            throw new ReglaDominioException("El apartamento debe estar activo y PREPARADO");
        }
        return apartamento;
    }
}
