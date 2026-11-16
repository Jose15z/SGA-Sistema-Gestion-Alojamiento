package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Disponibilidad de un apartamento (3.3, 7.5).
 *
 * <p>Aplica las seis validaciones de creación de 7.5 en su orden: (1) salida posterior
 * a la entrada — la garantiza {@link Estancia} —, (2) entrada no anterior a hoy (RN-04),
 * (3) apartamento activo y con tarifas completas (7.4), (4) capacidad (RN-02),
 * (5) sin reservas activas ni bloqueos solapados (RN-01, RN-07) y (6) tiempo de
 * preparación respecto de la estancia anterior y la siguiente (RN-20).</p>
 *
 * <p>Ningún agregado puede decidirlo solo: requiere las demás reservas y los bloqueos.</p>
 */
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

    /**
     * Verifica las condiciones de venta en el orden de 7.5.
     *
     * @param reservaExcluida reserva que se modifica y no compite consigo misma (null al crear)
     * @param hoy             fecha actual para RN-04 (null cuando no aplica)
     * @throws ReglaDominioException con la primera condición que no se cumple
     */
    public void verificarDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                    CodigoReserva reservaExcluida, LocalDate hoy) {
        if (apartamento == null || estancia == null || totalOcupantes < 1) {
            throw new ReglaDominioException("La disponibilidad requiere apartamento, estancia y ocupantes");
        }
        // (2) RN-04
        if (hoy != null && estancia.fechaEntrada().isBefore(hoy)) {
            throw new ReglaDominioException("No se pueden crear reservas cuya fecha de entrada esté en el pasado");
        }
        // (3) 7.4
        if (!apartamento.estaActivo()) {
            throw new ReglaDominioException("El apartamento no está activo para la venta");
        }
        if (!apartamento.tieneTarifasCompletas()) {
            throw new ReglaDominioException("El apartamento no tiene tarifa en todas las temporadas");
        }
        // (4) RN-02
        if (!apartamento.admite(totalOcupantes)) {
            throw new ReglaDominioException("El grupo excede la capacidad del apartamento");
        }
        List<Reserva> otras = reservas.buscarActivasPorApartamento(apartamento.getIdentificacion()).stream()
                .filter(otra -> !otra.getCodigo().equals(reservaExcluida))
                .toList();
        // (5) RN-01 y RN-07
        if (otras.stream().anyMatch(otra -> otra.getEstancia().seSolapaCon(estancia))) {
            throw new ReglaDominioException("Existe una reserva activa en esas noches");
        }
        for (Bloqueo bloqueo : bloqueos.buscarVigentesPorApartamento(apartamento.getIdentificacion())) {
            if (bloqueo.afecta(estancia)) {
                throw new ReglaDominioException("Hay un bloqueo vigente: " + bloqueo.getMotivo());
            }
        }
        // (6) RN-20
        TiempoPreparacion tiempo = configuracion.tiempoPreparacion();
        if (tiempo == null) {
            throw new ReglaDominioException("Falta configurar el tiempo de preparación");
        }
        for (Reserva otra : otras) {
            Estancia existente = otra.getEstancia();
            Estancia anterior = existente.fechaSalida().isAfter(estancia.fechaEntrada()) ? estancia : existente;
            Estancia siguiente = anterior == existente ? estancia : existente;
            Duration disponible = Duration.between(anterior.fechaSalida().atTime(tiempo.horaSalida()),
                    siguiente.fechaEntrada().atTime(tiempo.horaEntrada()));
            if (disponible.compareTo(Duration.ofHours(tiempo.horas())) < 0) {
                throw new ReglaDominioException("No se cumple el tiempo de preparación entre reservas");
            }
        }
    }

    /** Variante sin la condición de fecha actual (RN-04). */
    public void verificarDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                    CodigoReserva reservaExcluida) {
        verificarDisponible(apartamento, estancia, totalOcupantes, reservaExcluida, null);
    }

    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes, LocalDate hoy) {
        try {
            verificarDisponible(apartamento, estancia, totalOcupantes, null, hoy);
            return true;
        } catch (ReglaDominioException e) {
            return false;
        }
    }

    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes) {
        return estaDisponible(apartamento, estancia, totalOcupantes, null);
    }

    /**
     * Calendario de ocupación del apartamento en [desde, hasta) (7.5): cada noche
     * está reservada, bloqueada, en preparación, no vendible o disponible.
     */
    public List<NocheCalendario> calendario(Apartamento apartamento, Estancia rango) {
        if (apartamento == null || rango == null) {
            throw new ReglaDominioException("El calendario requiere apartamento y rango de fechas");
        }
        List<Reserva> activas = reservas.buscarActivasPorApartamento(apartamento.getIdentificacion());
        List<Bloqueo> vigentes = bloqueos.buscarVigentesPorApartamento(apartamento.getIdentificacion());
        TiempoPreparacion tiempo = configuracion.tiempoPreparacion();
        List<NocheCalendario> noches = new ArrayList<>();
        for (LocalDate noche : rango.nochesOcupadas()) {
            Estancia unaNoche = new Estancia(noche, noche.plusDays(1));
            Reserva ocupante = activas.stream().filter(r -> r.getEstancia().incluye(noche)).findFirst().orElse(null);
            if (ocupante != null) {
                noches.add(new NocheCalendario(noche, EstadoNoche.RESERVADA, ocupante.getCodigo()));
            } else if (vigentes.stream().anyMatch(b -> b.afecta(unaNoche))) {
                noches.add(new NocheCalendario(noche, EstadoNoche.BLOQUEADA, null));
            } else if (!apartamento.estaActivo()) {
                noches.add(new NocheCalendario(noche, EstadoNoche.NO_VENDIBLE, null));
            } else if (!tiempo.permiteEntradaElMismoDia()
                    && activas.stream().anyMatch(r -> r.getEstancia().fechaSalida().equals(noche))) {
                noches.add(new NocheCalendario(noche, EstadoNoche.EN_PREPARACION, null));
            } else {
                noches.add(new NocheCalendario(noche, EstadoNoche.DISPONIBLE, null));
            }
        }
        return noches;
    }
}
