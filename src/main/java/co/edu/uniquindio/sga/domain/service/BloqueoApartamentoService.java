package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.Objects;

/**
 * 7.3 con RN-01 y RN-07: no puede registrarse un bloqueo sobre noches que
 * ya tengan reservas activas, ni duplicar un bloqueo vigente.
 *
 * <p>El bloqueo no conoce las reservas y la reserva no conoce los bloqueos.</p>
 */
public final class BloqueoApartamentoService {

    private final ReservaRepository reservas;
    private final BloqueoRepository bloqueos;

    public BloqueoApartamentoService(ReservaRepository reservas, BloqueoRepository bloqueos) {
        this.reservas = Objects.requireNonNull(reservas);
        this.bloqueos = Objects.requireNonNull(bloqueos);
    }

    public void validarBloqueo(IdentificacionApartamento apartamento, Estancia periodo) {
        if (apartamento == null || periodo == null) {
            throw new ReglaDominioException("El bloqueo requiere apartamento y periodo");
        }
        boolean conReservas = reservas.buscarActivasPorApartamento(apartamento).stream()
                .anyMatch(reserva -> reserva.getEstancia().seSolapaCon(periodo));
        if (conReservas) {
            throw new ReglaDominioException("No se puede bloquear noches que ya tienen reservas activas");
        }
        boolean conBloqueo = bloqueos.buscarVigentesPorApartamento(apartamento).stream()
                .anyMatch(bloqueo -> bloqueo.afecta(periodo));
        if (conBloqueo) {
            throw new ReglaDominioException("Ya existe un bloqueo vigente sobre esas noches");
        }
    }
}
