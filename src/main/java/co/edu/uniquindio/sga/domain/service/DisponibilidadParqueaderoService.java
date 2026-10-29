package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadParqueadero;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * RP-02: cada reserva incluye un cupo de parqueadero; el vehículo adicional
 * depende de que haya cupos libres en todas sus noches y genera un cargo por noche.
 *
 * <p>La reserva controla su propio máximo de vehículos, pero no sabe cuántos
 * cupos ocupan las demás reservas en las mismas noches.</p>
 */
public final class DisponibilidadParqueaderoService {

    private final ReservaRepository reservas;
    private final ConfiguracionAlojamiento configuracion;

    public DisponibilidadParqueaderoService(ReservaRepository reservas, ConfiguracionAlojamiento configuracion) {
        this.reservas = Objects.requireNonNull(reservas);
        this.configuracion = Objects.requireNonNull(configuracion);
    }

    /**
     * Verifica el cupo para un vehículo adicional de la reserva.
     *
     * @return cargo por todas las noches de la estancia
     * @throws ReglaDominioException si alguna noche no tiene cupo
     */
    public Dinero verificarVehiculoAdicional(Reserva reserva) {
        if (reserva == null) {
            throw new ReglaDominioException("La reserva es obligatoria");
        }
        CapacidadParqueadero capacidad = configuracion.capacidadParqueadero();
        List<Reserva> otras = reservas.buscarActivasEnPeriodo(reserva.getEstancia()).stream()
                .filter(otra -> !otra.equals(reserva))
                .toList();
        for (LocalDate noche : reserva.getEstancia().nochesOcupadas()) {
            int ocupados = otras.stream()
                    .filter(otra -> otra.getEstancia().incluye(noche))
                    .mapToInt(otra -> otra.getVehiculos().size())
                    .sum();
            int solicitados = reserva.getVehiculos().size() + 1;
            if (!capacidad.admite(ocupados + solicitados)) {
                throw new ReglaDominioException(
                        "No hay cupo de parqueadero para un vehículo adicional la noche del " + noche);
            }
        }
        return configuracion.tarifaVehiculoAdicionalPorNoche().por(reserva.getEstancia().noches());
    }
}
