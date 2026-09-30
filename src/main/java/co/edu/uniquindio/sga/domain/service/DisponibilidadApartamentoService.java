package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.Duration;
import java.util.Objects;

/** RN-01/02/07/20: verifica capacidad, noches, bloqueos y preparación en ambos sentidos. */
public final class DisponibilidadApartamentoService {
    private final ReservaRepository reservas;
    private final BloqueoRepository bloqueos;
    private final ConfiguracionAlojamiento configuracion;
    public DisponibilidadApartamentoService(ReservaRepository reservas, BloqueoRepository bloqueos,
                                             ConfiguracionAlojamiento configuracion) {
        this.reservas = Objects.requireNonNull(reservas);
        this.bloqueos = Objects.requireNonNull(bloqueos);
        this.configuracion = Objects.requireNonNull(configuracion);
    }
    public void verificarDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                    CodigoReserva reservaExcluida) {
        if (apartamento == null || estancia == null || totalOcupantes < 1) {
            throw new ReglaDominioException("La disponibilidad requiere apartamento, estancia y ocupantes");
        }
        if (!apartamento.estaActivo()) throw new ReglaDominioException("El apartamento no está activo para la venta");
        if (!apartamento.admite(totalOcupantes)) throw new ReglaDominioException("El grupo excede la capacidad del apartamento");
        TiempoPreparacion tiempo = configuracion.tiempoPreparacion();
        if (tiempo == null) throw new ReglaDominioException("Falta configurar el tiempo de preparación");
        for (Reserva otra : reservas.buscarActivasPorApartamento(apartamento.getIdentificacion())) {
            if (otra.getCodigo().equals(reservaExcluida)) continue;
            Estancia existente = otra.getEstancia();
            if (existente.seSolapaCon(estancia)) throw new ReglaDominioException("Existe una reserva activa en esas noches");
            Estancia anterior = existente.fechaSalida().isAfter(estancia.fechaEntrada()) ? estancia : existente;
            Estancia siguiente = anterior == existente ? estancia : existente;
            Duration disponible = Duration.between(anterior.fechaSalida().atTime(tiempo.horaSalida()),
                    siguiente.fechaEntrada().atTime(tiempo.horaEntrada()));
            if (disponible.compareTo(Duration.ofHours(tiempo.horas())) < 0) {
                throw new ReglaDominioException("No se cumple el tiempo de preparación entre reservas");
            }
        }
        for (Bloqueo bloqueo : bloqueos.buscarVigentesPorApartamento(apartamento.getIdentificacion())) {
            if (bloqueo.afecta(estancia)) throw new ReglaDominioException("Hay un bloqueo vigente: " + bloqueo.getMotivo());
        }
    }
    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes) {
        try { verificarDisponible(apartamento, estancia, totalOcupantes, null); return true; }
        catch (ReglaDominioException e) { return false; }
    }
}
